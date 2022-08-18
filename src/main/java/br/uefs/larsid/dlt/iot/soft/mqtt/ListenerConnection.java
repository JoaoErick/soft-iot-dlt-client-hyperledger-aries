package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;

import java.util.Base64;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ListenerConnection implements IMqttMessageListener {

  /*-------------------------Constantes--------------------------------------*/
  private static final String CONNECT = "SYN";
  private static final String DISCONNECT = "FIN";
  /*-------------------------------------------------------------------------*/

  /* -------------------------- Aries Topic constants ----------------------- */
  private static final String CREATE_INVITATION = "POST CREATE_INVITATION";
  private static final String CREDENTIAL_DEFINITIONS = "POST CREDENTIAL_DEFINITIONS";
  private static final String ISSUE_CREDENTIAL = "POST ISSUE_CREDENTIAL";
  /* ----------------------------------------------------------------------- */

  /* -------------------------- Aries Topic Res constants ------------------ */
  private static final String CREATE_INVITATION_RES = "CREATE_INVITATION_RES";
  private static final String CREDENTIAL_DEFINITIONS_RES = "CREDENTIAL_DEFINITIONS_RES";
  private static final String ISSUE_CREDENTIAL_RES = "ISSUE_CREDENTIAL_RES";
  /* ----------------------------------------------------------------------- */

  /* -------------------------- Nodes Topic constants ----------------------- */
  private static final String SEND_INVITATION = "POST SEND_INVITATION";
  /* ----------------------------------------------------------------------- */

  private static final int QOS = 1;

  private boolean debugModeValue;
  private Controller controllerImpl;
  private MQTTClient MQTTClientHost;
  private MQTTClient MQTTClientUp;

  /**
   * Método Construtor.
   *
   * @param controllerImpl Controller - Controller que fará uso desse Listener.
   * @param MQTTClientHost MQTTClient - Cliente MQTT do gateway inferior.
   * @param topics         String[] - Tópicos que serão assinados.
   * @param qos            int - Qualidade de serviço do tópico que será ouvido.
   * @param debugModeValue boolean - Modo para debugar o código.
   */
  public ListenerConnection(
      Controller controllerImpl,
      MQTTClient MQTTClientHost,
      MQTTClient MQTTClientUp,
      String[] topics,
      int qos,
      boolean debugModeValue) {
    this.controllerImpl = controllerImpl;
    this.MQTTClientHost = MQTTClientHost;
    this.MQTTClientUp = MQTTClientUp;
    this.debugModeValue = debugModeValue;

    for (String topic : topics) {
      this.MQTTClientHost.subscribe(qos, this, topic);
    }
  }

  @Override
  public void messageArrived(String topic, MqttMessage message)
      throws Exception {
    final String[] params = topic.split("/");
    String msg = new String(message.getPayload());

    printlnDebug("==== Receive Connect Request ====");

    /* Verificar qual o tópico recebido. */
    switch (params[0]) {
      case CONNECT:
        printlnDebug("CREATE_INVITATION...");
        this.controllerImpl.addNodeUri(msg);
        
        sendToControllerAries(CREATE_INVITATION, "");

        break;
      case DISCONNECT:
        this.controllerImpl.removeNodeUri(msg);

        break;
      case CREATE_INVITATION_RES:
        printlnDebug("CREATE_INVITATION_RES...");
        printlnDebug(msg);

        publishToDown(SEND_INVITATION, msg.getBytes());

        break;
    }
  }

  private void sendToControllerAries(String topic, String message) {
    byte[] payload = message.getBytes();
    this.MQTTClientHost.publish(topic, payload, QOS);
  }

  private void publishToDown(String topicDown, byte[] messageDown) {
    List<String> nodesUris = this.controllerImpl.getNodeUriList();
    String user = this.MQTTClientUp.getUserName();
    String password = this.MQTTClientUp.getPassword();

    String uri[] = nodesUris.get(nodesUris.size() - 1).split(":");

    MQTTClient MQTTClientDown = new MQTTClient(
        this.debugModeValue,
        uri[0],
        uri[1],
        user,
        password);

    MQTTClientDown.connect();
    MQTTClientDown.publish(topicDown, messageDown, QOS);
    MQTTClientDown.disconnect();
  }

  private void printlnDebug(String str) {
    if (isDebugModeValue()) {
      System.out.println(str);
    }
  }

  public boolean isDebugModeValue() {
    return debugModeValue;
  }

  public void setDebugModeValue(boolean debugModeValue) {
    this.debugModeValue = debugModeValue;
  }
}
