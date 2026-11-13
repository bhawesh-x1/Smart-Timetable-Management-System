package edu.piet.smartcampus;

import edu.piet.smartcampus.runtime.SmartCampusDesktopApp;

/**
 * Standard Java Project Entry Point (Main.java).
 * Run this file directly in IntelliJ IDEA, VS Code, or via CLI (`java edu.piet.smartcampus.Main`)
 * to start the Smart Campus server and launch both the Desktop Application Window & Browser Hub.
 */
public class Main {

    public static void main(String[] args) {
        SmartCampusDesktopApp.launch(args);
    }
}
