/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.1-SNAPSHOT).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package info.glennengstrand.resources;

import java.util.List;
import info.glennengstrand.api.Friend;
import info.glennengstrand.api.Inbound;
import info.glennengstrand.api.Outbound;
import info.glennengstrand.api.Participant;

public interface ParticipantApi extends DateAware {
      Friend addFriend(Long id, Friend body);
      Outbound addOutbound(Long id, Outbound body);
      Participant addParticipant(Participant body);
      List<Friend> getFriend(Long id);
      List<Inbound> getInbound(Long id);
      List<Outbound> getOutbound(Long id);
      Participant getParticipant(Long id);

}
