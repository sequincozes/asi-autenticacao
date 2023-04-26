package ex2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Scanner;

public class Cliente {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 1234;
    private static final String SECRET_KEY = "mysecretkey";

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
            System.out.println("Conectado ao PDV (servidor) em " + SERVER_ADDRESS + ":" + PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            // Solicita ao usuário o número da comanda
            System.out.print("Número da comanda: ");
            String commandNumber = scanner.nextLine();

            // Envia o número da comanda para o servidor e recebe o valor da comanda e um número aleatório (nonce)
            out.println(commandNumber);
            String[] response = in.readLine().split(" ");
            int value = Integer.parseInt(response[0]);
            int nonce = Integer.parseInt(response[1]);

            // Calcula o hash MD5 (H1) a partir da concatenação da chave secreta (SECRET_KEY) com o nonce recebido do servidor
            String H1 = calculateHash(SECRET_KEY + nonce);

            // Gera um novo nonce (N2)
            Random random = new Random();
            int N2 = random.nextInt();

            // Envia H1 e N2 para o servidor
            out.println(H1);
            out.println(N2);

            // Recebe H2 do servidor
            String H2 = in.readLine();

            // Calcula o hash MD5 (H2_check) a partir da concatenação da chave secreta (SECRET_KEY) com o novo nonce (N2)
            String H2_check = calculateHash(SECRET_KEY + N2);

            // Verifica se H2 bate com H2_check
            if (!H2.equals(H2_check)) {
                System.out.println("Hashes incompatíveis. Pagamento recusado.");
                return;
            }

            // Solicita ao usuário que autorize ou recuse o pagamento
            System.out.print("Autorizar pagamento? (s/n) ");
            String authorize = scanner.nextLine();
            if (authorize.equals("s")) {
                out.println("Autoriza Pagamento");
                System.out.println("Pagamento autorizado.");
            } else {
                out.println("Recusa Pagamento");
                System.out.println("Pagamento recusado.");
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // Método para calcular o hash MD5 de uma string
    private static String calculateHash(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
