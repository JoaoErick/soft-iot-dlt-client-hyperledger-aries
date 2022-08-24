package br.uefs.larsid.dlt.iot.soft.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ListenerInvitation implements IMqttMessageListener {

  /* -------------------------- Aries Topic constants ----------------------- */
  private static final String ACCEPT_INVITATION = "POST ACCEPT_INVITATION";
  private static final String ISSUE_CREDENTIAL = "POST ISSUE_CREDENTIAL";
  /* ----------------------------------------------------------------------- */

  /* -------------------------- Aries Topic Res constants ------------------ */
  private static final String CREDENTIAL_DEFINITIONS_RES = "CREDENTIAL_DEFINITIONS_RES";
  private static final String ISSUE_CREDENTIAL_RES = "ISSUE_CREDENTIAL_RES";
  /* ----------------------------------------------------------------------- */

  /* -------------------------- Nodes Topic constants ----------------------- */
  private static final String SEND_INVITATION = "POST SEND_INVITATION";
  /* ----------------------------------------------------------------------- */

  private static final int QOS = 1;

  private boolean debugModeValue;
  private MQTTClient MQTTClientHost;

  /**
   * Método Construtor.
   *
   * @param MQTTClientHost MQTTClient - Cliente MQTT do gateway inferior.
   * @param topics         String[] - Tópicos que serão assinados.
   * @param qos            int - Qualidade de serviço do tópico que será ouvido.
   * @param debugModeValue boolean - Modo para debugar o código.
   */
  public ListenerInvitation(
      MQTTClient MQTTClientHost,
      String[] topics,
      int qos,
      boolean debugModeValue) {
    this.MQTTClientHost = MQTTClientHost;
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
    }
  }

  private void sendToControllerAries(String topic, String message) {
    byte[] payload = message.getBytes();
    this.MQTTClientHost.publish(topic, payload, QOS);
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
