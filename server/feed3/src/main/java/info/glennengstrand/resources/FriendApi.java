/**
 * News Feed
 * news feed api
 *
 * OpenAPI spec version: 1.0.0
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 *
 * Licensed under the Eclipse Public License - v 1.0
 *
 * https://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.glennengstrand.resources;

import com.google.inject.Inject;  
import javax.ws.rs.GET;  
import javax.ws.rs.POST;  
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import java.util.List;
import io.dropwizard.jersey.params.LongParam;

import info.glennengstrand.api.Friend;
import info.glennengstrand.api.Participant;

@Path("/participant/{id}")
public class FriendApi {

   private final FriendApiService friendService;
   private final ParticipantApi.ParticipantApiService participantService;
	
   @Inject
   public FriendApi(FriendApiService friendService, ParticipantApi.ParticipantApiService participantService) {
      this.friendService = friendService;
      this.participantService = participantService;
   }
    
   @GET
   @Produces("application/json")
  /**
   * retrieve an individual participant
   * fetch a participant by id
   * @param id uniquely identifies the participant (required)
   * @return Participant
   */
   public Participant getParticipant(@PathParam("id") LongParam id) {
      return participantService.getParticipant(id.get());
   }
    
   @POST
   @Path("/friends")
   @Consumes("application/json")
   @Produces("application/json")
  /**
   * create a new friendship
   * friends are those participants who receive news
   * @param id uniquely identifies the participant (required)
   * @param body friendship to be created (required)
   * @return Friend
   */
   public Friend addFriend(@PathParam("id") LongParam id,  Friend body) {
      return friendService.addFriend(id.get(), body);
   }
   @GET
   @Path("/friends")
   @Produces("application/json")
  /**
   * retrieve the list of friends for an individual participant
   * fetch participant friends
   * @param id uniquely identifies the participant (required)
   * @return List<Friend>
   */
   public List<Friend> getFriend(@PathParam("id") LongParam id) {
      return friendService.getFriend(id.get());
   }
   public static interface FriendApiService {
      Friend addFriend(Long id, Friend body);
      List<Friend> getFriend(Long id);
   }
}
