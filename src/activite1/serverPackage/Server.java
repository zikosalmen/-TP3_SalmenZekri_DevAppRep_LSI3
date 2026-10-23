package activite1.serverPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TP 3 - Activité 3-1 : Serveur Multi-threads TCP (Chaînes de Caractères)
 * 
 * Classe Server :
 * - Écoute sur le port 1234
 * - Accepte les connexions entrantes de plusieurs clients simultanément
 * - Attribue un numéro d'ordre séquentiel à chaque client (Client n°1, n°2, ...)
 * - Affiche les informations de connexion (adresse IP distante via getRemoteSocketAddress(), numéro d'ordre)
 * - Délègue le traitement de chaque client à un thread indépendant (ClientProcess)
 */
public class Server {

    private static final int PORT = 1234;
    private static int clientCount = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("  SERVEUR MULTI-THREADS TCP (Activité 3-1 : Multi-clients)");
        System.out.println("==========================================================");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Serveur] Serveur multi-thread démarré avec succès.");
            System.out.println("[Serveur] En attente de connexions clientes sur le port " + PORT + "...\n");

            while (true) {
                // Attente passive d'une nouvelle connexion cliente
                Socket clientSocket = serverSocket.accept();

                // Incrémentation et attribution du numéro d'ordre de connexion
                clientCount++;
                int currentClientNumber = clientCount;

                // Affichage côté serveur des informations du client connecté
                System.out.println("----------------------------------------------------------");
                System.out.println("[Serveur] Nouvelle connexion acceptée !");
                System.out.println("  -> Numéro d'ordre : Client n°" + currentClientNumber);
                System.out.println("  -> Adresse distante (IP:port) : " + clientSocket.getRemoteSocketAddress());
                System.out.println("  -> Adresse Inet : " + clientSocket.getInetAddress().getHostAddress());
                System.out.println("  -> Port distant : " + clientSocket.getPort());
                System.out.println("  -> Nombre total de clients accueillis : " + currentClientNumber);
                System.out.println("----------------------------------------------------------");

                // Instanciation et démarrage du thread dédié pour le client
                ClientProcess process = new ClientProcess(clientSocket, currentClientNumber);
                process.start();
            }

        } catch (IOException e) {
            System.err.println("[Serveur] Erreur critique au niveau du ServerSocket : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
