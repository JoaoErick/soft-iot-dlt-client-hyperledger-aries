package br.uefs.larsid.dlt.iot.soft.model;

import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerConnection;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerCredentialDefinition;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerInvitation;
import br.uefs.larsid.dlt.iot.soft.mqtt.MQTTClient;
import br.uefs.larsid.dlt.iot.soft.services.Controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ControllerImpl implements Controller {

  /*--------------------------Constants-------------------------------------*/
  private static final int QOS = 1;
  
  private static final String CONNECT = "SYN";
  private static final String DISCONNECT = "FIN";
  /*-------------------------------------------------------------------------*/

  /* -------------------------- Aries Topic constants ---------------------- */
  private static final String CREDENTIAL_DEFINITIONS = "POST CREDENTIAL_DEFINITIONS";
  private static final String CREATE_INVITATION = "POST CREATE_INVITATION";
  /* ----------------------------------------------------------------------- */

  /* -------------------------- Aries Topic Res constants ------------------ */
  private static final String CREATE_INVITATION_RES = "CREATE_INVITATION_RES";
  private static final String ACCEPT_INVITATION_RES = "ACCEPT_INVITATION_RES";
  private static final String CREDENTIAL_DEFINITIONS_RES = "CREDENTIAL_DEFINITIONS_RES";
  /* ----------------------------------------------------------------------- */

  private boolean debugModeValue;
  private boolean hasNodes;
  private MQTTClient MQTTClientUp;
  private MQTTClient MQTTClientHost;
  private List<String> nodesUris;
  private int timeoutInSeconds;
  private JsonObject sensorsTypesJSON = new JsonObject();
  private boolean crendentialDefinitionIsConfigured = false;
  private Map<String, String> connectionIdNodes = new LinkedHashMap<String, String>();

  public Map<String, String> getConnectionIdNodes() {
    return connectionIdNodes;
  }

  public void addConnectionIdNodes(String nodeUri, String connectionId) {
    this.connectionIdNodes.put(nodeUri, connectionId);
  }

  public ControllerImpl() {}

  /**
   * Inicializa o bundle.
   */
  public void start() {
    this.MQTTClientUp.connect();
    this.MQTTClientHost.connect();

    if (hasNodes) {
      nodesUris = new ArrayList<>();
      String[] topicsConnection = { CONNECT, DISCONNECT };
      String[] topicsCredentialDefinition = { CREDENTIAL_DEFINITIONS_RES };
      String[] topicsInvitation = { ACCEPT_INVITATION_RES };

      new ListenerConnection(
        this,
        MQTTClientHost,
        MQTTClientUp,
        topicsConnection,
        QOS,
        debugModeValue
      );

      new ListenerCredentialDefinition(
        this,
        MQTTClientHost,
        topicsCredentialDefinition,
        QOS,
        debugModeValue
      );

      new ListenerInvitation(
        this,
        MQTTClientHost,
        MQTTClientUp,
        topicsInvitation,
        QOS,
        debugModeValue
      );

      byte[] payload = "".getBytes();

      this.MQTTClientHost.publish(CREDENTIAL_DEFINITIONS, payload, QOS);
      
    } else {
      String[] topics = { CREATE_INVITATION_RES };

      new ListenerInvitation(
        this,
        MQTTClientHost,
        MQTTClientUp,
        topics,
        QOS,
        debugModeValue
      );

      byte[] payload = String
        .format("%s:%s", MQTTClientHost.getIp(), MQTTClientHost.getPort())
        .getBytes();

      this.MQTTClientHost.publish(CREATE_INVITATION, payload, QOS);
    }
  }

  /**
   * Finaliza o bundle.
   */
  public void stop() {
    if (!this.hasNodes) {
      byte[] payload = String
        .format("%s:%s", MQTTClientHost.getIp(), MQTTClientHost.getPort())
        .getBytes();

      this.MQTTClientUp.publish(DISCONNECT, payload, QOS);
      this.MQTTClientHost.unsubscribe(CREATE_INVITATION_RES);

    } else {
      this.MQTTClientUp.unsubscribe(CONNECT);
      this.MQTTClientUp.unsubscribe(DISCONNECT);
      this.MQTTClientHost.unsubscribe(CREDENTIAL_DEFINITIONS_RES);
      this.MQTTClientHost.unsubscribe(ACCEPT_INVITATION_RES);
    }

    this.MQTTClientHost.disconnect();
    this.MQTTClientUp.disconnect();
  }

  /**
   * Adiciona os dispositivos que foram requisitados na lista de dispositivos.
   */
  // @Override
  // public void loadConnectedDevices() {
  //   this.loadConnectedDevices(ClientIotService.getApiIot(this.urlAPI));
  // }

  /**
   * Adiciona os dispositivos que foram requisitados na lista de dispositivos.
   *
   * @param strDevices String - Dispositivos requisitados.
   */
  // private void loadConnectedDevices(String strDevices) {
  //   List<Device> devicesTemp = new ArrayList<Device>();

  //   try {
  //     printlnDebug("JSON load:");
  //     printlnDebug(strDevices);

  //     JSONArray jsonArrayDevices = new JSONArray(strDevices);

  //     for (int i = 0; i < jsonArrayDevices.length(); i++) {
  //       JSONObject jsonDevice = jsonArrayDevices.getJSONObject(i);
  //       ObjectMapper mapper = new ObjectMapper();
  //       Device device = mapper.readValue(jsonDevice.toString(), Device.class);

  //       devicesTemp.add(device);

  //       List<Sensor> tempSensors = new ArrayList<Sensor>();
  //       JSONArray jsonArraySensors = jsonDevice.getJSONArray("sensors");

  //       for (int j = 0; j < jsonArraySensors.length(); j++) {
  //         JSONObject jsonSensor = jsonArraySensors.getJSONObject(j);
  //         Sensor sensor = mapper.readValue(jsonSensor.toString(), Sensor.class);
  //         sensor.setUrlAPI(urlAPI);
  //         tempSensors.add(sensor);
  //       }

  //       device.setSensors(tempSensors);
  //     }
  //   } catch (JsonParseException e) {
  //     e.printStackTrace();
  //     printlnDebug(
  //       "Verify the correct format of 'DevicesConnected' property in configuration file."
  //     );
  //   } catch (JsonMappingException e) {
  //     e.printStackTrace();
  //     printlnDebug(
  //       "Verify the correct format of 'DevicesConnected' property in configuration file."
  //     );
  //   } catch (IOException e) {
  //     e.printStackTrace();
  //   }

  //   this.devices = devicesTemp;

  //   printlnDebug("Amount of devices connected: " + this.devices.size());
  // }

  /**
   * Calcula o score dos dispositivos conectados.
   *
   * @return Map
   */
  // @Override
  // public Map<String, Integer> calculateScores(JsonArray functionHealth) {
  //   Map<String, Integer> temp = new LinkedHashMap<String, Integer>();
  //   List<String> sensorsTypes = this.loadSensorsTypes();

  //   if (sensorsTypes.size() == functionHealth.size()) {
  //     for (int i = 0; i < functionHealth.size(); i++) {
  //       String sensorType = functionHealth
  //         .get(i)
  //         .getAsJsonObject()
  //         .get("sensor")
  //         .getAsString();

  //       /**
  //        * Caso o tipo de sensor especificado não exista nos dispositivos,
  //        * retorna um Map vazio.
  //        */
  //       if (!sensorsTypes.contains(sensorType)) {
  //         return new LinkedHashMap<String, Integer>();
  //       }
  //     }

  //     for (Device device : this.devices) {
  //       int score = 0;
  //       int sumWeight = 0;

  //       for (int i = 0; i < functionHealth.size(); i++) {
  //         String sensorType = functionHealth
  //           .get(i)
  //           .getAsJsonObject()
  //           .get("sensor")
  //           .getAsString();
  //         int weight = functionHealth
  //           .get(i)
  //           .getAsJsonObject()
  //           .get("weight")
  //           .getAsInt();

  //         Sensor sensor = device.getSensorBySensorType(sensorType);
  //         sensor.getValue(device.getId());
  //         score += sensor.getValue() * weight;
  //         sumWeight += weight;
  //       }

  //       temp.put(device.getId(), score / sumWeight);
  //     }
  //   }

  //   return temp;
  // }

  /**
   * Publica o Top-K calculado para a camada de cima.
   *
   * @param id String - Id da requisição.
   * @param k  int - Quantidade de scores requisitados.
   * @param functionHealth JsonArray - Array contendo a função de cálculo do
   * Top-K.
   */
  // @Override
  // public void publishTopK(String id, int k, JsonArray functionHealth) {
  //   printlnDebug("Waiting for Gateway nodes to send their Top-K");

  //   long start = System.currentTimeMillis();
  //   long end = start + this.timeoutInSeconds * 1000;

  //   if (this.hasNodes) {
  //     /*
  //      * Enquanto a quantidade de respostas da requisição for menor que o número
  //      * de nós filhos
  //      */
  //     while (
  //       this.responseQueue.get(id) < this.nodesUris.size() &&
  //       System.currentTimeMillis() < end
  //     ) {}
  //   }

  //   /*
  //    * Consumindo apiIot para pegar os valores mais atualizados dos
  //    * dispositivos.
  //    */
  //   // this.loadConnectedDevices();

  //   // if (!this.devices.isEmpty()) {
  //   //   /* Adicionando os dispositivos conectados em si mesmo. */
  //   //   this.putScores(id, this.calculateScores(functionHealth));

  //   //   int topkMapSize = this.topKScores.get(id).size();

  //   //   if (topkMapSize < k) {
  //   //     printlnDebug("Insufficient Top-K!");

  //   //     this.sendInvalidTopKMessage(
  //   //         id,
  //   //         String.format(
  //   //           "Can't possible calculate the Top-%s, sending the Top-%d!",
  //   //           k,
  //   //           topkMapSize
  //   //         )
  //   //       );
  //   //   }
  //   // }

  //   printlnDebug("OK... now let's calculate the TOP-K of TOP-K's!");

  //   /*
  //    * Reordenando o mapa de Top-K (Ex: {device2=23, device1=14}) e
  //    * atribuindo-o à carga de mensagem do MQTT
  //    */
  //   Map<String, Integer> topK = SortTopK.sortTopK(
  //     this.getMapById(id),
  //     k,
  //     debugModeValue
  //   );

  //   printlnDebug("Top-K Result => " + topK.toString());
  //   printlnDebug("==== Fog gateway -> Cloud gateway  ====");

  //   JsonObject json = new JsonObject();
  //   // json.addProperty("id", id);
  //   // json.addProperty("timestamp", System.currentTimeMillis());

  //   String deviceListJson = new Gson().toJson(MapToArray.mapToArray(topK));

  //   json.addProperty("devices", deviceListJson);

  //   byte[] payload = json.toString().replace("\\", "").getBytes();

  //   MQTTClientUp.publish(TOP_K_RES_FOG + id, payload, 1);

  //   // this.removeRequest(id);
  //   // this.removeSpecificResponse(id);
  // }

  /**
   * Publica os tipos de sensores para a camada de cima.
   */
  @Override
  public void publishSensorType() {
    printlnDebug("Waiting for Gateway nodes to send their sensors types");

    long start = System.currentTimeMillis();
    long end = start + this.timeoutInSeconds * 1000;

    /*
     * Enquanto a quantidade de respostas da requisição for menor que o número
     * de nós filhos
     */
    while (
      System.currentTimeMillis() < end
    ) {}

    byte[] payload = sensorsTypesJSON.toString().replace("\\", "").getBytes();

    // MQTTClientUp.publish(SENSORS_FOG_RES, payload, 1);

    // this.removeSpecificResponse("getSensors");
  }

  /**
   * Requisita os tipos de sensores de um dispositivo conectado.
   *
   * @return List<String>
   */
  // public List<String> loadSensorsTypes() {
  //   List<String> sensorsList = new ArrayList<>();

  //   for (Sensor sensor : this.getDevices().get(0).getSensors()) {
  //     sensorsList.add(sensor.getType());
  //   }

  //   return sensorsList;
  // }

  /**
   * Retorna o mapa de scores de acordo com o id da requisição
   * passado por parâmetro.
   *
   * @param id String - Id da requisição.
   * @return Map
   */
  // @Override
  // public Map<String, Integer> getMapById(String id) {
  //   return this.topKScores.get(id);
  // }

  /**
   * Adiciona um mapa de scores de uma nova requisição no mapa de
   * requisições na sua respectiva.
   *
   * @param id     String - Id da requisição.
   * @param fogMap Map - Mapa de requisições.
   */
  // @Override
  // public void putScores(String id, Map<String, Integer> fogMap) {
  //   if (this.topKScores.get(id) != null) {
  //     this.topKScores.get(id).putAll(fogMap);
  //   } else {
  //     this.topKScores.put(id, fogMap).isEmpty();
  //   }
  // }

  /**
   * Adiciona os sensores em um JSON para enviar para a camada superior.
   *
   * @param jsonReceived JSONObject - JSON contendo os tipos dos sensores.
   */
  // @Override
  // public void putSensorsTypes(JsonObject jsonReceived) {
  //   if (this.sensorsTypesJSON.get("sensors").getAsString().equals("[]")) {
  //     sensorsTypesJSON = jsonReceived;
  //   }
  // }

  /**
   * Envia uma mensagem indicando que o Top-K pedido possui uma quantidade
   * inválida.
   *
   * @param topicId String - Id da requisição do Top-K.
   * @param message String - Mensagem.
   */
  // @Override
  // public void sendInvalidTopKMessage(String topicId, String message) {
  //   printlnDebug(message);

  //   MQTTClientUp.publish(INVALID_TOP_K_FOG + topicId, message.getBytes(), QOS);
  // }

  /**
   * Remove do mapa de requisições o id da requisição junto com mapa de scores
   * associado a ele.
   *
   * @param id String - Id da requisição.
   */
  // @Override
  // public void removeRequest(String id) {
  //   this.topKScores.remove(id);
  // }

  /**
   * Cria uma nova chave no mapa de resposta dos filhos.
   *
   * @param id String - Id da requisição.
   */
  // @Override
  // public void addResponse(String id) {
  //   responseQueue.put(id, 0);
  // }

  /**
   * Atualiza a quantidade de respostas.
   *
   * @param id String - Id da requisição.
   */
  // @Override
  // public void updateResponse(String id) {
  //   int temp = responseQueue.get(id);
  //   responseQueue.put(id, ++temp);
  // }

  /**
   * Remove uma resposta específica da fila de respostas.
   *
   * @param id String - Id da requisição.
   */
  // @Override
  // public void removeSpecificResponse(String id) {
  //   responseQueue.remove(id);
  // }

  /**
   * Envia um mapa vazio.
   *
   * @param topicId String - Id da requisição.
   */
  @Override
  public void sendEmptyTopK(String topicId) {
    byte[] payload = new LinkedHashMap<String, Map<String, Integer>>()
      .toString()
      .getBytes();

    // this.MQTTClientUp.publish(TOP_K_RES_FOG + topicId, payload, QOS);
  }

  /**
   * Adiciona um URI na lista de URIs.
   *
   * @param uri String - URI que deseja adicionar.
   */
  @Override
  public void addNodeUri(String uri) {
    if (!this.nodesUris.contains(uri)) {
      this.nodesUris.add(uri);
    }

    printlnDebug(String.format("URI: %s added in the nodesIps list.", uri));
    this.showNodesConnected();
  }

  /**
   * Remove uma URI na lista de URIs.
   *
   * @param uri String - URI que deseja remover.
   */
  @Override
  public void removeNodeUri(String uri) {
    int pos = this.findNodeUri(uri);

    if (pos != -1) {
      this.nodesUris.remove(pos);

      printlnDebug(String.format("URI: %s removed in the nodesIps list.", uri));

      this.showNodesConnected();
    } else {
      printlnDebug("Error, the desired node was not found.");
    }
  }

  /**
   * Retorna a posição de um URI na lista de URIs
   *
   * @param uri String - URI que deseja a posição.
   * @return int
   */
  private int findNodeUri(String uri) {
    for (int pos = 0; pos < this.nodesUris.size(); pos++) {
      if (this.nodesUris.get(pos).equals(uri)) {
        return pos;
      }
    }

    return -1;
  }

  /**
   * Retorna a lista de URIs dos nós conectados.
   *
   * @return List
   */
  @Override
  public List<String> getNodeUriList() {
    return this.nodesUris;
  }

  /**
   * Retorna a quantidade de nós conectados.
   *
   * @return String
   */
  @Override
  public int getNodes() {
    return this.nodesUris.size();
  }

  /**
   * Exibe a URI dos nós que estão conectados.
   */
  public void showNodesConnected() {
    printlnDebug("+---- Nodes URI Connected ----+");
    for (String nodeIp : this.getNodeUriList()) {
      printlnDebug("     " + nodeIp);
    }

    if (this.getNodeUriList().size() == 0) {
      printlnDebug("        empty");
    }
    printlnDebug("+----------------------------+");
  }

  private void printlnDebug(String str) {
    if (debugModeValue) {
      System.out.println(str);
    }
  }

  public boolean isDebugModeValue() {
    return this.debugModeValue;
  }

  public void setDebugModeValue(boolean debugModeValue) {
    this.debugModeValue = debugModeValue;
  }

  public MQTTClient getMQTTClientUp() {
    return this.MQTTClientUp;
  }

  public void setMQTTClientUp(MQTTClient MQTTClientUp) {
    this.MQTTClientUp = MQTTClientUp;
  }

  public MQTTClient getMQTTClientHost() {
    return this.MQTTClientHost;
  }

  public void setMQTTClientHost(MQTTClient mQTTClientHost) {
    this.MQTTClientHost = mQTTClientHost;
  }

  public List<String> getNodesUris() {
    return nodesUris;
  }

  public void setNodesUris(List<String> nodesUris) {
    this.nodesUris = nodesUris;
  }

  /**
   * Verifica se o gateway possui filhos.
   *
   * @return boolean
   */
  @Override
  public boolean hasNodes() {
    return hasNodes;
  }

  public void setHasNodes(boolean hasNodes) {
    this.hasNodes = hasNodes;
  }

  public boolean crendentialDefinitionIsConfigured() {
    return crendentialDefinitionIsConfigured;
  }

  public void setCrendentialDefinitionIsConfigured(boolean crendentialDefinitionIsConfigured) {
    this.crendentialDefinitionIsConfigured = crendentialDefinitionIsConfigured;
  }

  public void setTimeoutInSeconds(int timeoutInSeconds) {
    this.timeoutInSeconds = timeoutInSeconds;
  }

  /**
   * Retorna um JSON contendo os tipos de sensores disponíveis.
   *
   * @return JsonObject
   */
  // @Override
  // public JsonObject getSensorsTypesJSON() {
  //   return sensorsTypesJSON;
  // }
}
