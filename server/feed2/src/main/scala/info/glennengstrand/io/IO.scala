package info.glennengstrand.io

import java.text.{SimpleDateFormat, DateFormat}
import java.util.logging.{Logger, Level}

import com.mchange.v2.c3p0.ComboPooledDataSource
import java.util.{Calendar, Date, Properties}
import scala.util.parsing.json.JSON
import java.sql.{SQLException, PreparedStatement, Connection}

object IO {
  val settings = new Properties
  val df = new SimpleDateFormat("yyyy-MM-dd")
  val log = Logger.getLogger("info.glennengstrand.io.IO")
  val jdbcVendor = "jdbc_vendor"
  val jdbcDriveName = "jdbc_driver"
  val jdbcUrl = "jdbc_url"
  val jdbcUser = "jdbc_user"
  val jdbcPassword = "jdbc_password"
  val jdbcMinPoolSize = "jdbc_min_pool_size"
  val jdbcMaxPoolSize = "jdbc_max_pool_size"
  val jdbcAcquireIncrement = "jdbc_acquire_increment"
  val jdbcMaxStatements = "jdbc_max_statements"
  val nosqlHost = "nosql_host"
  val nosqlKeyspace = "nosql_keyspace"
  val nosqlReadConsistencyLevel = "nosql_read_consistency_level"
  val nosqlTimeToLiveInSeconds = "nosql_ttl"
  val messagingBrokers = "messaging_brokers"
  val zookeeperServers = "zookeeper_servers"
  val searchHost = "search_host"
  val cacheConfig = "cache_config"

  val sql: scala.collection.mutable.Map[String, PreparedStatement] = scala.collection.mutable.Map()

  var cacheStatements = true
  var unitTesting = false

  def cacheAwareRead(o: PersistentDataStoreBindings, criteria: Map[String, Any], reader: PersistentDataStoreReader, cache: CacheAware): Iterable[Map[String, Any]] = {
    def loadFromDbAndCache: Iterable[Map[String, Any]] = {
      val fromDb = reader.read(o, criteria)
      fromDb.size match {
        case 0 =>
        case _ => cache.store(o, fromDb, criteria)
      }
      fromDb
    }
    val fromCache = cache.load(o, criteria)
    fromCache.size match {
      case 0 => loadFromDbAndCache
      case _ => fromCache
    }
  }
  def toJsonValue(v: Any): String = {
    v match {
      case l: Long => l.toString
      case i: Int => i.toString
      case s: String => "\"" + s + "\""
      case o: MicroServiceSerializable => o.toJson
      case d: Date => "\"" + df.format(d) + "\""
      case _ => "\"" + v.toString + "\""
    }
  }
  def toJson(state: Map[String, Any]): String = {
    val s = state.map(kv => "\"" + kv._1 + "\":" + toJsonValue(kv._2)).reduce(_ + "," + _)
    "{" + s + "}"
  }
  def toJson(state: Iterable[Map[String, Any]]): String = {
    val s = state.map{ li => {
      "{" +li.map(kv => "\"" + kv._1 + "\":" +  toJsonValue(kv._2)).reduce(_ + "," + _) + "}"
    }}.reduce(_ + "," + _)
    "[" + s + "]"
  }
  def fromJson(json: String): Iterable[Map[String, Option[Any]]] = {
    val retVal = JSON.parseFull(json).getOrElse(List())
    retVal match {
      case l: List[Map[String, Option[Any]]] => l
      case m: Map[String, Option[Any]] => List(m)
      case _ => List()
    }
  }
  def fromFormPost(state: String) : Map[String, Any] = {
    state.isEmpty match {
      case false => state.split("&").map(kv => kv.split("=")).map(t => (t(0), t(1))).toMap
      case _ => Map()
    }
  }
  def convertToLong(v: Any) : Long = {
    v match {
      case l: Long => l
      case d: Double => d.toLong
      case i: Int => i.toLong
      case s: String => s.toLong
    }
  }
  def convertToInt(v: Any) : Int = {
    v match {
      case l: Long => l.toInt
      case d: Double => d.toInt
      case i: Int => i
      case s: String => s.toInt
    }
  }
  def convertToDate(v: Any): Date = {
    v match {
      case l: Long => new Date(l)
      case d: Date => d
      case s: String => df.parse(s)
    }
  }
}

abstract class FactoryClass {
  def getObject(name: String, id: Long): Option[Object]
  def getObject(name: String, id: Int): Option[Object]
  def getObject(name: String, state: String): Option[Object]
  def getObject(name: String): Option[Object]
}

class EmptyFactoryClass extends FactoryClass {
  def getObject(name: String, id: Long): Option[Object] = {
    None
  }
  def getObject(name: String, id: Int): Option[Object] = {
    None
  }
  def getObject(name: String, state: String): Option[Object] = {
    None
  }
  def getObject(name: String): Option[Object] = {
    None
  }
}

abstract class PersistentDataStoreBindings {
  def entity: String
  def fetchInputs: Iterable[String]
  def fetchOutputs: Iterable[(String, String)]
  def fetchOrder: Map[String, String]
  def upsertInputs: Iterable[String]
  def upsertOutputs: Iterable[(String, String)]
  def getTypeOf(fieldName: String): String = {
    val retVal = for ((fn, ft) <- fetchOutputs if fieldName == fn) yield ft
    retVal.isEmpty match {
      case true => "Int"
      case _ => retVal.head
    }
  }
}

trait PersistentRelationalDataStoreStatementAware {
  def generatePreparedStatement(operation: String, entity: String, inputs: Iterable[String], outputs: Iterable[(String, String)]): String
  def reset: Unit = {
    IO.sql.synchronized {
      IO.sql.clear()
    }
  }
  def prepare(operation: String, entity: String, inputs: Iterable[String], outputs: Iterable[(String, String)], pool: PooledRelationalDataStore): PreparedStatement = {
    if (IO.cacheStatements) {
      val key = operation + ":" + entity
      IO.sql.contains(key) match {
        case false => {
          IO.sql.synchronized {
            IO.sql.contains(key) match {
              case false => {
                IO.unitTesting match {
                  case true => new MockPreparedStatement
                  case _ => {
                    val stmt = pool.getDbConnection.prepareStatement(generatePreparedStatement(operation, entity, inputs, outputs))
		    IO.sql.put(key, stmt)
		    stmt
                  }
                }
              }
              case true => IO.sql.get(key).get
            }
          }
        }
        case true => IO.sql.get(key).get
      }
    } else {
      IO.unitTesting match {
        case true => new MockPreparedStatement
        case _ => pool.getDbConnection.prepareStatement(generatePreparedStatement(operation, entity, inputs, outputs))
      }
    }
  }
}

trait PersistentDataStoreReader {
  def read(o: PersistentDataStoreBindings, criteria: Map[String, Any]): Iterable[Map[String, Any]]
}

trait PersistentRelationalDataStoreReader extends PersistentDataStoreReader with PooledRelationalDataStore with PersistentRelationalDataStoreStatementAware {
  val operation = "Fetch"
  def read(o: PersistentDataStoreBindings, criteria: Map[String, Any]): Iterable[Map[String, Any]] = {
    val stmt = prepare(operation, o.entity, o.fetchInputs, o.fetchOutputs, this)
    try {
      stmt.synchronized {
        Sql.prepare(stmt, o.fetchInputs, criteria)
        Sql.query(stmt, o.fetchOutputs)
      }
    } catch {
      case e: SQLException => {
        IO.log.log(Level.WARNING, "cannot fetch data\n", e)
        reset
        val stmt = prepare(operation, o.entity, o.fetchInputs, o.fetchOutputs, this)
        stmt.synchronized {
          Sql.prepare(stmt, o.fetchInputs, criteria)
          Sql.query(stmt, o.fetchOutputs)
        }
      }
    }
  }
}

trait PersistentDataStoreWriter {
  def write(o: PersistentDataStoreBindings, state: Map[String, Any], criteria: Map[String, Any]): Map[String, Any]
}

trait PersistentRelationalDataStoreWriter extends PersistentDataStoreWriter with PooledRelationalDataStore with PersistentRelationalDataStoreStatementAware {
  val operation = "Upsert"
  def write(o: PersistentDataStoreBindings, state: Map[String, Any], criteria: Map[String, Any]): Map[String, Any] = {
    val stmt = prepare(operation, o.entity, o.upsertInputs, o.upsertOutputs, this)
    try {
      stmt.synchronized {
        Sql.prepare(stmt, o.upsertInputs, state)
        Sql.execute(stmt, o.upsertOutputs).toMap[String, Any]
      }
    } catch {
      case e: SQLException => {
        IO.log.log(Level.WARNING, "cannot upsert data\n", e)
        reset
        val stmt = prepare(operation, o.entity, o.upsertInputs, o.upsertOutputs, this)
        stmt.synchronized {
          Sql.prepare(stmt, o.upsertInputs, state)
          Sql.execute(stmt, o.upsertOutputs).toMap[String, Any]
        }
      }
    }
  }
}

trait PersistentDataStoreSearcher {
  def search(terms: String): Iterable[java.lang.Long]
  def index(id: Long, content: String): Unit
}

trait PerformanceLogger {
  def logRecord(entity: String, operation: String, duration: Long): String = {
    val now = Calendar.getInstance()
    val ts = now.get(Calendar.YEAR).toString + "|" + now.get(Calendar.MONTH).toString + "|" + now.get(Calendar.DAY_OF_MONTH).toString + "|" + now.get(Calendar.HOUR_OF_DAY).toString + "|" + now.get(Calendar.MINUTE).toString
    ts + "|" + entity + "|" + operation + "|" + duration.toString
  }
  def log(topic: String, entity: String, operation: String, duration: Long): Unit
}

trait CacheAware {
  def load(o: PersistentDataStoreBindings, criteria: Map[String, Any]): Iterable[Map[String, Any]]
  def store(o: PersistentDataStoreBindings, state: Map[String, Any], criteria: Map[String, Any]): Unit
  def store(o: PersistentDataStoreBindings, state: Iterable[Map[String, Any]], criteria: Map[String, Any]): Unit
  def append(o: PersistentDataStoreBindings, state: Map[String, Any], criteria: Map[String, Any]): Unit
  def invalidate(o: PersistentDataStoreBindings, criteria: Map[String, Any]): Unit
}

trait MockCacheAware extends CacheAware {
  def load(o: PersistentDataStoreBindings, criteria: Map[String, Any]): Iterable[Map[String, Any]] = {
    // TODO: implement this
    List()
  }
  def store(o: PersistentDataStoreBindings, state: Map[String, Any], criteria: Map[String, Any]): Unit = {
    // TODO: implement this

  }
  def store(o: PersistentDataStoreBindings, state: Iterable[Map[String, Any]], criteria: Map[String, Any]): Unit = {
    // TODO: implement this

  }
  def append(o: PersistentDataStoreBindings, state: Map[String, Any], criteria: Map[String, Any]): Unit = {
    // TODO: implement this

  }
  def invalidate(o: PersistentDataStoreBindings, criteria: Map[String, Any]): Unit = {
    // TODO: implement this

  }
}

class MockCache extends MockCacheAware

trait MicroServiceSerializable {
  def toJson: String
  def toJson(factory: FactoryClass): String
}
