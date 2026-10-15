package com.exemplo;

import com.rabbitmq.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class SupportConsumerApp {
    private static final String EXCHANGE_NAME = "app_topic_exchange";
    private static final String QUEUE_NAME = "support_queue";
    private static final String DLX_NAME = "dlx_exchange";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv().getOrDefault("RABBITMQ_HOST", "localhost"));
        Thread.sleep(10000); // Aguarda o RabbitMQ inicializar

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Configuração da Dead Letter Exchange (DLX) e associação à fila de suporte
        Map<String, Object> argsQueue = new HashMap<>();
        argsQueue.put("x-dead-letter-exchange", DLX_NAME);
        argsQueue.put("x-dead-letter-routing-key", "dead-letter");

        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
        channel.queueDeclare(QUEUE_NAME, true, false, false, argsQueue);
        // Vincula a fila à routing key exigida pelo trabalho
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "support.request");

        System.out.println(" [*] Consumidor de Suporte aguardando mensagens...");

        ObjectMapper mapper = new ObjectMapper();

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String messageStr = new String(delivery.getBody(), "UTF-8");
            try {
                Message msg = mapper.readValue(messageStr, Message.class);

                // Processamento propositalmente mais lento para acumular na fila e aparecer no painel do RabbitMQ
                Thread.sleep(600);

                // Classificação da solicitação de suporte nas categorias pedidas no trabalho
                String categoria = classificarSuporte(msg.getContent());
                System.out.println(" [IA 2 - Suporte] Categoria: " + categoria + " | Texto: " + msg.getContent());

                // Confirmação manual da mensagem (manual ACK)
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            } catch (Exception e) {
                System.err.println(" [X] Erro no processamento do suporte. Enviando para DLQ...");
                // Rejeita sem reenfileirar, enviando para a DLQ
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            }
        };

        // Consumo com ACK manual ativado (autoAck = false)
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});
    }

    private static String classificarSuporte(String texto) {
        String lower = texto.toLowerCase();
        if (lower.contains("login") || lower.contains("senha") || lower.contains("conta")) {
            return "Problema de acesso";
        }
        if (lower.contains("lento") || lower.contains("travando") || lower.contains("desempenho")) {
            return "Problema de desempenho";
        }
        if (lower.contains("conexão") || lower.contains("rede") || lower.contains("caiu")) {
            return "Problema de conexão";
        }
        return "Problema de acesso"; // Categoria padrão caso não coincida
    }
}