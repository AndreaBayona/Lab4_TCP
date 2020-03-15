package test;

import cliente.Cliente;
import uniandes.gload.core.Task;

public class ClientServerTask extends Task {

    @Override
    public void fail() {
        System.out.println(Task.MENSAJE_FAIL);

    }

    @Override
    public void success() {
        System.out.println(Task.OK_MESSAGE);

    }

    @Override
    public void execute() {
        Cliente cliente = new Cliente();
        cliente.run();

    }

}
