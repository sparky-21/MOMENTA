package com.momenta.app;

/**
 * Java 11+ এ যদি main() ওয়ালা ক্লাস সরাসরি javafx.application.Application
 * extend করে, আর classpath দিয়ে (module-path ছাড়া) রান করা হয়, তাহলে
 * "JavaFX runtime components are missing" এরর আসে — যদিও jar ঠিকই থাকে।
 * এই আলাদা Launcher ক্লাসটা সেই চেক এড়িয়ে যায়, কারণ এটা নিজে
 * Application extend করে না, শুধু Main.main() কে কল করে।
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}