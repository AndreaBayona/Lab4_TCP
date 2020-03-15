package test;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

public class Generator {

    private LoadGenerator generator;

    public Generator() {
        Task work = createTask();
        int numberOfTasks=10;
        int gap=100;
        generator= new LoadGenerator("Cliente-Servidor load test", numberOfTasks, work, gap);
        generator.generate();
    }

    private Task createTask() {
        return new ClientServerTask();
    }

    public static void main(String[] args) {
        @SuppressWarnings("unused")
        Generator gen = new Generator();
    }

}