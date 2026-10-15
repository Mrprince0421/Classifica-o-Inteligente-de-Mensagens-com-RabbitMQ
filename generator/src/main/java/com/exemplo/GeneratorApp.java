package com.exemplo;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Random;

public class GeneratorApp {
    private static final String EXCHANGE_NAME = "app_topic_exchange";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv().getOrDefault("RABBITMQ_HOST", "localhost"));

        // Aguarda a inicialização do RabbitMQ
        Thread.sleep(10000);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
            ObjectMapper mapper = new ObjectMapper();
            Random random = new Random();

            String[] reviews = {
                "Excelente produto, superou minhas expectativas!",
                "Péssima qualidade, chegou com defeito.",
                "O produto é razoável pelo preço cobrado."
            };

            String[] supports = {
                "Não consigo fazer login na minha conta.",
                "O sistema está muito lento e travando.",
                "Erro de conexão ao carregar os dados."
            };

            while (true) {
                // Publica 5 mensagens por segundo
                for (int i = 0; i < 5; i++) {
                    boolean isReview = random.nextBoolean();
                    String routingKey = isReview ? "review.product" : "support.request";
                    String text = isReview ? reviews[random.nextInt(reviews.length)] : supports[random.nextInt(supports.length)];

                    Message msg = new Message(routingKey, text);
                    String json = mapper.writeValueAsString(msg);

                    channel.basicPublish(EXCHANGE_NAME, routingKey, null, json.getBytes("UTF-8"));
                    System.out.println(" Publicado [" + routingKey + "]: " + text);
                }
                Thread.sleep(1000); // Garante a taxa de 5 msg/s
            }
        }
    }
}