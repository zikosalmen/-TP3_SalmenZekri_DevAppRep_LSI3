package activite2.clientPackage;

import activite2.model.Operation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TP 3 - Activité 3-2 : Serveur Multi-threads TCP (Calculatrice par Objets)
 * 
 * Classe Client :
 * - Spécifie l'adresse du serveur distant à l'aide de InetAddress et InetSocketAddress
 * - Établit la connexion avec le serveur multi-thread
 * - Utilise ObjectOutputStream et ObjectInputStream pour échanger des objets Operation sérialisés
 * - Valide la syntaxe locale avant la transmission
 * - Reçoit le résultat calculé sous forme d'objet et l'affiche à l'écran
 */
public class Client {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 1234;

    private static final Pattern OPERATION_PATTERN = Pattern.compile(
            "^\\s*(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(-?\\d+(?:\\.\\d+)?)\\s*$"
    );

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("  CLIENT CALCULATRICE TCP (Activité 3-2 : Objets)");
        System.out.println("==========================================================");

        Scanner scanner = new Scanner(System.in);

        // Configuration interactive pour tests locaux ou réseau local (multi-machines)
        System.out.print("Entrez l'adresse IP / hôte du serveur (défaut '" + DEFAULT_HOST + "') : ");
        String hostInput = scanner.nextLine().trim();
        String host = hostInput.isEmpty() ? DEFAULT_HOST : hostInput;

        System.out.print("Entrez le port du serveur (défaut " + DEFAULT_PORT + ") : ");
        String portInput = scanner.nextLine().trim();
        int port = portInput.isEmpty() ? DEFAULT_PORT : Integer.parseInt(portInput);

        try {
            // Utilisation explicite de InetAddress et InetSocketAddress (exigence énoncé)
            System.out.println("\n[Client] Résolution de l'adresse du serveur pour '" + host + "'...");
            InetAddress serverInetAddress = InetAddress.getByName(host);
            InetSocketAddress socketAddress = new InetSocketAddress(serverInetAddress, port);

            System.out.println("[Client] Connexion vers " + socketAddress + "...");
            Socket socket = new Socket();
            socket.connect(socketAddress, 5000);
            System.out.println("[Client] Connecté avec succès ! Adresse locale : " + socket.getLocalSocketAddress());

            // Initialisation des flux d'objets : ObjectOutputStream en premier + flush
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            // Lecture du message d'accueil initial (numéro d'ordre attribué au client)
            try {
                Object welcomeObj = ois.readObject();
                System.out.println("\n----------------------------------------------------------");
                System.out.println("[Serveur] " + welcomeObj);
                System.out.println("----------------------------------------------------------\n");
            } catch (ClassNotFoundException e) {
                System.err.println("[Client] Impossible de lire le message d'accueil : " + e.getMessage());
            }

            System.out.println("Opérations supportées : +, -, *, /");
            System.out.println("Exemples valides : '55 * 25', '100 - 45', '12.5 + 7.5', '20 / 4'");
            System.out.println("Pour quitter : tapez 'quitter', 'exit' ou '0'\n");

            while (true) {
                System.out.print("Entrez votre opération : ");
                String saisie = scanner.nextLine().trim();

                if (saisie.isEmpty()) {
                    continue;
                }

                // Vérification de déconnexion
                if (saisie.equalsIgnoreCase("quitter") || saisie.equalsIgnoreCase("exit") || saisie.equals("0")) {
                    System.out.println("[Client] Envoi de l'instruction de déconnexion...");
                    oos.writeObject(null);
                    oos.flush();
                    break;
                }

                // Validation syntaxique locale par expression régulière
                Matcher matcher = OPERATION_PATTERN.matcher(saisie);
                if (!matcher.matches()) {
                    System.err.println("[Client - Validation] ERREUR : Syntaxe d'opération invalide !");
                    System.err.println("  -> La saisie \"" + saisie + "\" a été rejetée localement sans émission réseau.");
                    System.err.println("  -> Respectez le format : <nombre> <opérateur> <nombre> (ex: 55 * 25).");
                    continue;
                }

                // Extraction des paramètres
                double op1 = Double.parseDouble(matcher.group(1));
                char operateur = matcher.group(2).charAt(0);
                double op2 = Double.parseDouble(matcher.group(3));

                // Instanciation de l'objet sérialisable
                Operation operation = new Operation(op1, operateur, op2);
                System.out.println("[Client] Émission de l'objet Operation : [" + op1 + " " + operateur + " " + op2 + "]");

                // Envoi de l'objet au serveur
                oos.writeObject(operation);
                oos.flush();
                oos.reset();

                // Réception de l'objet calculé
                try {
                    Object reponseObj = ois.readObject();
                    if (reponseObj instanceof Operation) {
                        Operation reponse = (Operation) reponseObj;
                        if (reponse.isErreur()) {
                            System.err.println("[Client] Notification reçue du serveur : " + reponse.getMessageErreur());
                        } else {
                            System.out.println("[Client] Résultat reçu : " + reponse);
                            System.out.println("         (Index d'opération globale serveur : #" + reponse.getOperationId() + ")");
                        }
                    } else {
                        System.err.println("[Client] Réponse de type inattendu reçue : " + reponseObj);
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("[Client] Impossible de désérialiser la réponse : " + e.getMessage());
                }

                System.out.println();
            }

            // Fermeture des ressources
            ois.close();
            oos.close();
            socket.close();
            System.out.println("[Client] Déconnexion effectuée. Programme terminé.");

        } catch (IOException e) {
            System.err.println("[Client] Erreur de communication : " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
