package com.company;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

class Card {
    int fill, count, shape, color;

    public Card() {
    }

    public Card(int fill, int count, int shape, int color) {
        this.fill = fill;
        this.count = count;
        this.shape = shape;
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return fill == card.fill &&
                count == card.count &&
                shape == card.shape &&
                color == card.color;
    }

    @Override
    public String toString() {
        return "Card [" +
                "count = " + count +
                ", fill = " + fill +
                ", shape = " + shape +
                ", color = " + color + "]";
    }

    public Card getThird(Card c) {
        Card thirdCard = new Card();
        if (this.fill == c.fill) thirdCard.fill = c.fill;
        else thirdCard.fill = 6 - (this.fill + c.fill);
        if (this.count == c.count) thirdCard.count = c.count;
        else thirdCard.count = 6 - (this.count + c.count);
        if (this.shape == c.shape) thirdCard.shape = c.shape;
        else thirdCard.shape = 6 - (this.shape + c.shape);
        if (this.color == c.color) thirdCard.color = c.color;
        else thirdCard.color = 6 - (this.color + c.color);
        return thirdCard;
    }

    public Card[] findSet(Card[] c) {
        Card[] res = new Card[3];
        for (int i = 0; i < c.length; i++) {
            if (c[i].equals(c[i + 1])) {
            }
        }
        return res;
    }
}

class Cards {
    ArrayList<Card> cards;

    public ArrayList<Card> getCards() {
        return cards;
    }
}

class Request {
    String action, nickname;
    int token;
    ArrayList<Card> cards;

    // Регистрация
    public Request(String action, String nickname, int token) {
        this.action = action;
        this.nickname = nickname;
        this.token = token;
    }

    // получение набора карт
    public Request(String action, int token) {
        this.action = action;
        this.token = token;
    }

    // Выбор сета
    public Request(String action, int token, ArrayList<Card> cards) {
        this.action = action;
        this.cards = cards;
        this.token = token;
    }
}

class Response {
    String status;
    int token, points, cards_left;
    ArrayList<Card> cards;
}

public class Main {
    private static final String set_server_url = "http://194.176.114.21:8051"; // изменить порт на нужный
    public static int token = -1;
    public static ArrayList<Card> c = new ArrayList<>();
    public static int cardsLeft = 81;
    public static Scanner s = new Scanner(System.in);
    public static String name;

    public static void main(String[] args) throws IOException {
        System.out.println("Зарегестрируйтесь. Введите своё имя: ");
        name = s.nextLine();
        registration(name);
        Request tC = new Request("fetch_cards", token);
        Response rC = serverRequest(tC);
        c = rC.cards;

        outerLoop:
        while (true) {
            try {
                while (true) {
                    Scanner sInd = new Scanner(System.in);
                    System.out.println("Меню действий: ");
                    displayMenu();
                    int choice = sInd.nextInt();
                    switch (choice) {
                        case 1: //вывести карты
                            Request takeCards = new Request("fetch_cards", token);
                            Response respCards = serverRequest(takeCards);
                            c = respCards.cards;
                            for (int i = 0; i < c.size(); i++) {
                                System.out.println((i + 1) + ") " + c.get(i).toString());
                            }
                            break;
                        case 2: //ввести сет
                            inputSet();
                            break;
                        case 3: //Выход
                            break outerLoop;
                    }
                    if (cardsLeft == 0) {
                        System.out.println("Вы собрали все сеты!");
                        break outerLoop;
                    }
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public static void inputSet() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Введите номера карт через пробел: ");
        String setCards = scan.nextLine();
        String[] subStr = setCards.split(" ");
        if (subStr.length != 3) {
            System.out.println("Неправильный ввод. Попробуйте заного:");
            inputSet();
        }
        ArrayList<Card> set = new ArrayList<>();
        for (String i : subStr) {
            int index = Integer.parseInt(i) - 1;
            set.add(c.get(index));
        }
        Request reqSet = new Request("take_set", token, set);
        Response resp = serverRequest(reqSet);
        assert resp != null;
        if (resp.status.equals("ok")) {
            System.out.println("Вы нашли сет!");
            System.out.println("Карты " + setCards + "удалены из набора");
            System.out.println("Карт осталось: " + resp.cards_left);
            System.out.println("Игрок (" + name + ") заработал: " + resp.points + " очка(ов)");
            cardsLeft = resp.cards_left;
        } else {
            System.out.println("Эти карты не являются сетом");
        }
    }

    public static Response serverRequest(Request req) {
        Gson gson = new Gson();
        try {
            URL url = new URL(set_server_url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            OutputStream out = urlConnection.getOutputStream();
            out.write(gson.toJson(req).getBytes());
            InputStream stream = urlConnection.getInputStream();
            return gson.fromJson(new InputStreamReader(stream), Response.class);
        } catch (ConnectException e) {
            System.out.println("Подключение к серверу отсутсвтует");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println("Неверный url сервера");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void registration(String name) {
        Request register = new Request("register", name, -1);
        Response resp = serverRequest(register);
        assert resp != null;
        token = resp.token;
        if (resp.status.equals("ok")) {
            System.out.println("Вы зарегистрировались на сервере под именем " + name + ". Ваш токен - " + token);
        } else {
            System.out.println("Это имя уже используется. Введите другое: ");
            name = s.nextLine();
            registration(name);
        }
    }

    private static void displayMenu() {
        String[] choices = new String[]{"Вывести все карты", "Ввести сет", "Выход из меню"};
        for (int i = 0; i < choices.length; i++) {
            System.out.println((i + 1) + ") " + choices[i]);
        }
    }
}
