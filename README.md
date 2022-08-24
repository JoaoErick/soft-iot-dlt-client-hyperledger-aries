# soft-iot-dlt-client-hyperledger-aries

O `soft-iot-dlt-client-hyperledger-aries` é o *bundle* genérico que pode atuar tanto na camada *Edge* quanto na *Fog*. Ele responsável por realizar requisições MQTT para o controlador de um *Aries Agent*.

## Modelo da arquitetura

<p align="center">
  <img src="./assets/Decentralized-identity-soft-iot.png" width="580px" />
</p>

## Comunicação
---

 ### `Definição de Credencial`
 A partir do momento em que o *bundle* iniciar, é enviada uma publicação MQTT para o Agent Aries Controller com objetivo criar a definição de credencial e armazená-la na *blockchain*.

 Propriedades da Definição de Credencial | Valor padrão 
------------|------------- 
| name | soft-iot-base |
| version | 1.0 |

 Atributos | Descrição 
------------|------------- 
| id | Identificador do *gateway* |

---

### `Estabelecimento de Conexão entre Agentes`
Assim que um *fog gateway* se conectar a um *edge gateway*, o *fog gateway* envia uma publicação MQTT para o *Agent Aries Controller* a fim de receber uma URL de conexão de seu agente. A partir disto, o *fog gateway* envia a URL para o *edge gateway* para o mesmo aceitar a URL conexão no seu próprio agente.

###### A URL de conexão é gerada pelo agente emissor em *base64* e decodificada no agente receptor. ######

---

### `Emissão de Credencial`
O *fog gateway* envia uma publicação MQTT para o Agent Aries Controller com objetivo do mesmo emitir uma credencial para um agente de um *edge gateway*.

---