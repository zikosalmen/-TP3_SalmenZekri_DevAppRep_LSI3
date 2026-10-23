package activite2.serverPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TP 3 - Activité 3-2 : Serveur Multi-threads TCP (Calculatrice par Objets)
 * 
 * Classe Server :
 * - Écoute sur le port 1234
 * - Accepte plusieurs connexions clientes simultanées
 * - Associe chaque client connecté à un thread dédié ClientProcess
 * - Gère une ressource critique partagée : un COMPTEUR GLOBAL SYNCHRONISÉ du nombre
 *   total d'opérations traitées par l'ensemble des clients.
 * - Utilise le mot-clé 'synchronized' pour garantir l'exclusion mutuelle et
 *   empêcher les conflits d'accès concurrents (Race Conditions).
 */
public class Server {

    private static final int PORT = 1234;

    // Compteur de clients connectés
    private static int clientCount = 0;

    // RESSOURCE CRITIQUE PARTAGÉE : Nombre total d'opérations traitées par le serveur
    private static int totalOperationsCount = 0;

    // Verrou (lock) pour la synchronisation
    private static final Object lockCompteur = new Object();

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("  SERVEUR MULTI-THREADS TCP (Activité 3-2 : Calculatrice Objets)");
        System.out.println("==========================================================");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Serveur] Démarré avec succès sur le port " + PORT);
            System.out.println("[Serveur] Compteur global d'opérations initialisé à : 0");
            System.out.println("[Serveur] En attente de connexions clientes...\n");

            while (true) {
                // Attente passive de connexion
                Socket socket = serverSocket.accept();

                // Incrémentation du numéro de client
                clientCount++;
                int currentClientNumber = clientCount;

                System.out.println("----------------------------------------------------------");
                System.out.println("[Serveur] Nouveau client connecté !");
                System.out.println("  -> Client n°" + currentClientNumber);
                System.out.println("  -> Adresse distante : " + socket.getRemoteSocketAddress());
                System.out.println("----------------------------------------------------------");

                // Démarrage du thread de traitement client
                ClientProcess process = new ClientProcess(socket, currentClientNumber);
                process.start();
            }

        } catch (IOException e) {
            System.err.println("[Serveur] Erreur socket : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Méthode synchronisée permettant d'incrémenter de manière atomique
     * le compteur global partagé d'opérations.
     * 
     * Cette synchronisation garantit qu'un seul thread ClientProcess à la fois
     * peut modifier la valeur de totalOperationsCount, éliminant ainsi les
     * risques d'entrelacement destructeur (Race Condition / Perte de mise à jour).
     * 
     * @param clientNumber Numéro du client à l'origine de l'opération
     * @param descriptionOp Description textuelle de l'opération calculée
     * @return La nouvelle valeur globale du compteur
     */
    public static int incrementerCompteurGlobal(int clientNumber, String descriptionOp) {
        synchronized (lockCompteur) {
            totalOperationsCount++;
            int currentVal = totalOperationsCount;

            // Affichage dans la console du serveur de la nouvelle valeur du compteur
            System.out.println(">>> [ACCÈS CONCURRENT SYNCHRONISÉ] <<<");
            System.out.println("  Opération traitée pour Client n°" + clientNumber + " : " + descriptionOp);
            System.out.println("  -> NOUVELLE VALEUR DU COMPTEUR GLOBAL = " + currentVal);
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            return currentVal;
        }
    }

    /**
     * Récupère de façon synchronisée la valeur actuelle du compteur.
     */
    public static int getTotalOperationsCount() {
        synchronized (lockCompteur) {
            return totalOperationsCount;
        }
    }
}
