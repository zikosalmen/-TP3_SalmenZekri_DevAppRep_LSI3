package activite1.clientPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * TP 3 - Activité 3-1 : Serveur Multi-threads TCP (Chaînes de Caractères)
 * 
 * Classe Client :
 * - Utilise les classes InetAddress et InetSocketAddress pour spécifier l'adresse IP
 *   du serveur distant ou local (support réseau local et tests distants).
 * - Reçoit son numéro d'ordre de communication dès l'établissement de la connexion.
 * - Valide la syntaxe de l'opération en local via une regex stricte avant tout envoi réseau.
 * - Échange des chaînes de caractères via BufferedReader et PrintWriter.
 */
public class Client {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 1234;

    // Expression régulière stricte pour valider la syntaxe arithmétique avant émission
    private static final Pattern OPERATION_PATTERN = Pattern.compile(
            "^\\s*(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(-?\\d+(?:\\.\\d+)?)\\s*$"
    );

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("   CLIENT CALCULATRICE TCP (Activité 3-1 : Multi-clients)");
        System.out.println("==========================================================");

        Scanner scanner = new Scanner(System.in);

        // Configuration interactive de l'adresse du serveur (pour tests locaux ou réseau local)
        System.out.print("Entrez l'adresse IP / hôte du serveur (défaut '" + DEFAULT_HOST + "') : ");
        String hostInput = scanner.nextLine().trim();
        String host = hostInput.isEmpty() ? DEFAULT_HOST : hostInput;

        System.out.print("Entrez le port du serveur (défaut " + DEFAULT_PORT + ") : ");
        String portInput = scanner.nextLine().trim();
        int port = portInput.isEmpty() ? DEFAULT_PORT : Integer.parseInt(portInput);

        try {
            // Utilisation explicite des classes InetAddress et InetSocketAddress (exigence énoncé)
            System.out.println("\n[Client] Résolution de l'adresse IP pour '" + host + "'...");
            InetAddress serverInetAddress = InetAddress.getByName(host);
            InetSocketAddress socketAddress = new InetSocketAddress(serverInetAddress, port);

            System.out.println("[Client] Tentative de connexion vers " + socketAddress + "...");
            Socket socket = new Socket();
            socket.connect(socketAddress, 5000); // Délai d'attente max : 5 secondes

            System.out.println("[Client] Connecté avec succès ! Adresse locale : " + socket.getLocalSocketAddress());

            // Initialisation des flux de communication
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Réception et affichage du message d'accueil et du numéro d'ordre
            System.out.println("\n--- Message du Serveur ---");
            String bannerLine;
            // Lire le bloc d'accueil envoyé par le serveur (jusqu'à la consigne d'opération)
            while ((bannerLine = in.readLine()) != null) {
                System.out.println(bannerLine);
                if (bannerLine.contains("Entrez vos opérations") || bannerLine.contains("quitter")) {
                    break;
                }
            }
            System.out.println("--------------------------\n");

            // Boucle principale d'échange d'opérations
            while (true) {
                System.out.print("Entrez une opération (ex: 55 * 25) ou 'quitter' : ");
                String saisie = scanner.nextLine().trim();

                if (saisie.isEmpty()) {
                    continue;
                }

                // Vérification de sortie
                if (saisie.equalsIgnoreCase("quitter") || saisie.equalsIgnoreCase("exit") || saisie.equals("0")) {
                    out.println("quitter");
                    String reponseFin = in.readLine();
                    if (reponseFin != null) {
                        System.out.println("[Serveur] " + reponseFin);
                    }
                    System.out.println("[Client] Déconnexion demandée. Arrêt du client.");
                    break;
                }

                // Validation syntaxique locale avant envoi
                if (!validerSyntaxe(saisie)) {
                    System.err.println("[Client - Validation] ERREUR : Syntaxe de l'opération incorrecte !");
                    System.err.println("  -> La chaîne \"" + saisie + "\" a été bloquée localement.");
                    System.err.println("  -> Format requis : <nombre> <opérateur> <nombre> (ex: 55 * 25 ou 100 - 45).");
                    continue;
                }

                // Émission vers le serveur
                System.out.println("[Client] Envoi de la requête au serveur : \"" + saisie + "\"");
                out.println(saisie);

                // Réception de la réponse du serveur
                String reponse = in.readLine();
                if (reponse == null) {
                    System.out.println("[Client] Le serveur a fermé la connexion.");
                    break;
                }

                System.out.println("[Client] Résultat reçu : " + reponse + "\n");
            }

            // Fermeture des flux et de la socket
            in.close();
            out.close();
            socket.close();
            System.out.println("[Client] Socket fermée. Programme terminé.");

        } catch (IOException e) {
            System.err.println("[Client] Erreur de communication ou serveur inaccessible : " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    /**
     * Valide la syntaxe de l'opération en local à l'aide d'une regex.
     */
    public static boolean validerSyntaxe(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        return OPERATION_PATTERN.matcher(expression.trim()).matches();
    }
}
