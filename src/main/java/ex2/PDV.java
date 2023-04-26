package ex2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class PDV {
    private static final int PORT = 1234;
    private static final String SECRET_KEY = "mysecretkey";
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("PDV (servidor) iniciado na porta " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conexão estabelecida com " + clientSocket.getInetAddress().getHostAddress());
                
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                
                // Recebe o número da comanda do cliente
                String commandNumber = in.readLine();
                
                // Gera um número aleatório (nonce) e envia para o cliente juntamente com o valor da comanda
                Random random = new Random();
                int nonce = random.nextInt();
                out.println(commandNumber + " " + nonce);
                
                // Recebe o hash MD5 (H1) do cliente e o novo nonce (N2)
                String H1 = in.readLine();
                int N2 = Integer.parseInt(in.readLine());
                
                // Calcula o hash MD5 (H2) a partir da concatenação da chave secreta (SECRET_KEY) com N2
                String H2_check = calculateHash(SECRET_KEY + N2);
                
                // Verifica se H1 bate com H1_check
                String H1_check = calculateHash(nonce + SECRET_KEY);
                if (!H1.equals(H1_check)) {
                    System.out.println("Hashes incompatíveis. Conexão encerrada.");
                    clientSocket.close();
                    continue;
                }
                
                // Envia H2 para o cliente
                out.println(H2_check);
                
                // Recebe a resposta do cliente (Autoriza Pagamento ou Recusa Pagamento)
                String response = in.readLine();
                if (response.equals("Autoriza Pagamento")) {
                    System.out.println("Pagamento recebido.");
                } else {
                    System.out.println("Pagamento negado pelo cliente.");
                }
                
                // Fecha a conexão com o cliente
                clientSocket.close();
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
