package testing;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class Bot extends TelegramLongPollingBot {

    private boolean screaming = false;
    private int wins = 0;
    private int losses = 0;
    private int draws = 0;

    Map<Integer, String> RPSConvert = new HashMap<>() {
        {
            put(0, "rock \uD83D\uDDFF");
            put(1, "paper \uD83D\uDCC3");
            put(2, "scissors ✂");
        }
    };

    InlineKeyboardButton next = InlineKeyboardButton.builder()
            .text("Next").callbackData("next")
            .build();

    InlineKeyboardButton back = InlineKeyboardButton.builder()
            .text("Back").callbackData("back")
            .build();

    InlineKeyboardButton url = InlineKeyboardButton.builder()
            .text("Tutorial")
            .url("https://core.telegram.org/bots/api")
            .build();

    InlineKeyboardButton rock = InlineKeyboardButton.builder()
            .text("Rock \uD83D\uDDFF")
            .callbackData("10")
            .build();

    InlineKeyboardButton paper = InlineKeyboardButton.builder()
            .text("Paper \uD83D\uDCC3")
            .callbackData("11")
            .build();

    InlineKeyboardButton scissors = InlineKeyboardButton.builder()
            .text("Scissors ✂")
            .callbackData("12")
            .build();

    private InlineKeyboardMarkup keyboardM1= InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(next)).build();

    private InlineKeyboardMarkup keyboardM2 = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(back))
            .keyboardRow(List.of(url))
            .build();

    private InlineKeyboardMarkup keyboardRPS = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(rock))
            .keyboardRow(List.of(paper))
            .keyboardRow(List.of(scissors))
            .build();

    @Override
    public String getBotUsername() {
        return "Алексей";
    }

    @Override
    public String getBotToken() {
        return "";
    }

    @Override
    public void onUpdateReceived(Update update) {
//        System.out.println(update.getMessage().getText());
        if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();

            String callbackData = query.getData();
            String queryId = query.getId();
            Long id = query.getFrom().getId();
            int messageId = query.getMessage().getMessageId();

//            System.out.println(callbackData);
//            System.out.println(queryId);
//            System.out.println(id);
//            System.out.println(messageId);

            buttonTap(id, queryId, callbackData, messageId);
            return;
        }
        Message currentMessage = update.getMessage();
        String message = currentMessage.getText();
        Long id = currentMessage.getFrom().getId();
        if (currentMessage.isCommand()) {
            switch (message) {
                case "/scream":
                    screaming = true;
                    break;
                case "/whisper":
                    screaming = false;
                    break;
                case "/menu":
                    sendMenu(id, "Menu 1", keyboardM1);
                    break;
                case "/rps":
                    String RPSMenuText = String.format("Rock-paper-scissors game!\n" +
                            "Wins: " + wins + "\nLosses: " + losses + "\nDraws: " + draws);
                    sendMenu(id, RPSMenuText, keyboardRPS);
                    break;
            }
            return;
        }
        if (screaming) {
            scream(id, message);
        } else {
            sendText(id, message);
        }
    }

    private void scream(Long id, String message) {
        sendText(id, message.toUpperCase());
    }

    public void sendMenu(Long id, String message, InlineKeyboardMarkup kb) {
        SendMessage sm = SendMessage.builder().chatId(id.toString())
                .parseMode("HTML").text(message)
                .replyMarkup(kb).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void buttonTap(Long id, String queryId, String data, int messageId) {
        EditMessageText newTxt = EditMessageText.builder()
                .chatId(id.toString())
                .messageId(messageId).text("").build();

        EditMessageReplyMarkup newKb = EditMessageReplyMarkup.builder()
                .chatId(id.toString()).messageId(messageId).build();

//        System.out.println(data);

        if (data.equals("next")) {
            newTxt.setText("Menu 2");
            newKb.setReplyMarkup(keyboardM2);
        } else if (data.equals("back")) {
            newTxt.setText("Menu 1");
            newKb.setReplyMarkup(keyboardM1);
        } else if (data.charAt(0) == '1') {
            int playerGuess = Integer.parseInt(data.substring(1));
            int computerGuess = (int) (Math.random() * 3);
            String res;
            if (playerGuess == computerGuess) {
                res = "Draw! Алексей guessed " + RPSConvert.get(computerGuess);
                draws++;
            } else {
                if (playerGuess == 0 && computerGuess == 1 ||
                    playerGuess == 1 && computerGuess == 2 ||
                    playerGuess == 2 && computerGuess == 0) {
                    res = "You lost! Алексей guessed " + RPSConvert.get(computerGuess);
                    losses++;
                } else {
                    res = "You won! Алексей guessed " + RPSConvert.get(computerGuess);
                    wins++;
                }
            }
            newKb.setReplyMarkup(keyboardRPS);
            String RPSMenuText = String.format("Rock-paper-scissors game!\n" +
                    "Wins: " + wins + "\nLosses: " + losses + "\nDraws: " + draws);
            newTxt.setText(RPSMenuText + "\n" + res);
        }

        AnswerCallbackQuery close = AnswerCallbackQuery.builder()
                .callbackQueryId(queryId).build();

        try {
            execute(close);
            execute(newTxt);
            execute(newKb);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendText(Long id, String message) {
        SendMessage msg = SendMessage.builder().chatId(id).text(message).build();
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
