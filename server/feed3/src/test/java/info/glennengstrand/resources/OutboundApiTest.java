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

import info.glennengstrand.api.Outbound;
import org.junit.Test;

/**
 * API tests for OutboundApi
 */
public class OutboundApiTest {

    private final OutboundApi api = null;

    
    /**
     * create a participant news item
     *
     * socially broadcast participant news
     *
     */
    @Test
    public void addOutboundTest() {
        Outbound body = null;
        // Outbound response = api.addOutbound(body);

        // TODO: test validations
    }
    
    /**
     * retrieve the news posted by an individual participant
     *
     * fetch a participant news
     *
     */
    @Test
    public void getOutboundTest() {
        Long id = null;
        // List<Outbound> response = api.getOutbound(id);

        // TODO: test validations
    }
    
    /**
     * create a participant news item
     *
     * keyword search of participant news
     *
     */
    @Test
    public void searchOutboundTest() {
        String keywords = null;
        // List<Outbound> response = api.searchOutbound(keywords);

        // TODO: test validations
    }
    
}