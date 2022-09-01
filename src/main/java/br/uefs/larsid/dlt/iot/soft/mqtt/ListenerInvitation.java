package br.uefs.larsid.dlt.iot.soft.mqtt;

import java.net.InetAddress;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonObject;

import br.uefs.larsid.dlt.iot.soft.services.Controller;

public class ListenerInvitation implements IMqttMessageListener {

  /* -------------------------- Aries Topic constants ----------------------- */
  private static final String ACCEPT_INVITATION = "POST ACCEPT_INVITATION";
  private static final String ISSUE_CREDENTIAL = "POST ISSUE_CREDENTIAL";
  /* ----------------------------------------------------------------------- */

  /* -------------------------- Aries Topic Res constants ------------------ */
  private static final String ACCEPT_INVITATION_RES = "ACCEPT_INVITATION_RES";
  private static final String ACCEPT_INVITATION_FOG_RES = "ACCEPT_INVITATION_FOG_RES";
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
   * @param MQTTClientUp   MQTTClient - Cliente MQTT do gateway superior.
   * @param topics         String[] - Tópicos que serão assinados.
   * @param qos            int - Qualidade de serviço do tópico que será ouvido.
   * @param debugModeValue boolean - Modo para debugar o código.
   */
  public ListenerInvitation(
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

    printlnDebug("==== Receive Invitation Request ====");

    /* Verificar qual o tópico recebido. */
    switch (params[0]) {
      case SEND_INVITATION:
        printlnDebug("SEND_INVITATION...");
        sendToControllerAries(ACCEPT_INVITATION, msg);

        break;
      case ACCEPT_INVITATION_RES:
        printlnDebug("ACCEPT_INVITATION_RES...");

        String ip = this.MQTTClientHost.getIp();
        String port = this.MQTTClientHost.getPort();
        String uri = ip + ":" + port;

        this.MQTTClientUp.publish(ACCEPT_INVITATION_FOG_RES, uri.getBytes(), QOS);

        break;
      case ACCEPT_INVITATION_FOG_RES:
        printlnDebug("ACCEPT_INVITATION_FOG_RES...");

        String json = 
        "{" + 
          "\"value\":\"" + msg.split(":")[0] + "\"," + 
          "\"connectionId\":\"" + this.controllerImpl.getConnectionIdNodes().get(msg) + "\"" +
        "}";
        printlnDebug("ConnectionId do ip recebido: " + this.controllerImpl.getConnectionIdNodes().get(msg));
        printlnDebug("json: " + json);
        sendToControllerAries(ISSUE_CREDENTIAL, json);

        break;
    }
  }

  private void sendToControllerAries(String topic, String message) {
    byte[] payload = message.getBytes();
    this.MQTTClientHost.publish(topic, payload, QOS);
  }

  private void publishToUp(String topicDown, String messageUp) {

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
