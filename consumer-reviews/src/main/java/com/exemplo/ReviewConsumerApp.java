package com.exemplo;

import com.rabbitmq.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class ReviewConsumerApp {
    private static final String EXCHANGE_NAME = "app_topic_exchange";
    private static final String QUEUE_NAME = "review_queue";
    private static final String DLX_NAME = "dlx_exchange";
    private static final String DLQ_NAME = "dead_letter_queue";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv().getOrDefault("RABBITMQ_HOST", "localhost"));
        Thread.sleep(10000); // Aguarda o RabbitMQ subir

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Configuração da Dead Letter Queue (DLQ)
        channel.exchangeDeclare(DLX_NAME, "direct", true);
        channel.queueDeclare(DLQ_NAME, true, false, false, null);
        channel.queueBind(DLQ_NAME, DLX_NAME, "dead-letter");

        Map<String, Object> argsQueue = new HashMap<>();
        argsQueue.put("x-dead-letter-exchange", DLX_NAME);
        argsQueue.put("x-dead-letter-routing-key", "dead-letter");

        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
        channel.queueDeclare(QUEUE_NAME, true, false, false, argsQueue);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "review.product");

        System.out.println(" [*] Consumidor de Avaliações aguardando mensagens...");

        ObjectMapper mapper = new ObjectMapper();

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String messageStr = new String(delivery.getBody(), "UTF-8");
            try {
                Message msg = mapper.readValue(messageStr, Message.class);

                // Simular erro propositado caso o texto contenha "ERRO" para testar a DLQ
                if (msg.getContent().contains("ERRO")) {
                    throw new RuntimeException("Erro forçado para DLQ");
                }

                // Processamento mais lento para permitir acúmulo na fila
                Thread.sleep(500);

                String sentimento = classificarSentimento(msg.getContent());
                System.out.println(" [IA 1 - Sentimento] Classificação: " + sentimento + " | Texto: " + msg.getContent());

                // Confirmação manual da mensagem (manual ACK)
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            } catch (Exception e) {
                System.err.println(" [X] Erro ao processar mensagem. Enviando para DLQ...");
                // Rejeita a mensagem sem reenfileirar (vai direto para a DLQ)
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            }
        };

        // Consumo com ACK manual ativado (autoAck = false)
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});
    }

    private static String classificarSentimento(String texto) {
        String lower = texto.toLowerCase();
        if (lower.contains("excelente") || lower.contains("ótimo") || lower.contains("superou")) return "POSITIVO";
        if (lower.contains("péssima") || lower.contains("ruim") || lower.contains("defeito")) return "NEGATIVO";
        return "NEUTRO";
    }
}