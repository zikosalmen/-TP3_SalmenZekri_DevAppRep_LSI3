package activite1.serverPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TP 3 - Activité 3-1 : Serveur Multi-threads TCP (Chaînes de Caractères)
 * 
 * Classe ClientProcess :
 * - Thread dédié au traitement d'un client connecté spécifique
 * - Échange de messages textuels via BufferedReader et PrintWriter
 * - Transmet au client son numéro d'ordre de connexion
 * - Reçoit, calcule et renvoie les résultats des opérations arithmétiques (+, -, *, /)
 * - Maintient la communication de manière indépendante sans bloquer les autres clients
 */
public class ClientProcess extends Thread {

    private final Socket socket;
    private final int clientNumber;
    private final SocketAddress remoteAddress;

    // Pattern regex pour parser l'opération mathématique reçue
    private static final Pattern OPERATION_PATTERN = Pattern.compile(
            "^\\s*(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/])\\s*(-?\\d+(?:\\.\\d+)?)\\s*$"
    );

    public ClientProcess(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        this.remoteAddress = socket.getRemoteSocketAddress();
    }

    @Override
    public void run() {
        System.out.println("[Thread-" + clientNumber + "] Démarrage du thread pour le Client n°" 
                + clientNumber + " (" + remoteAddress + ")");

        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Étape 2.1.c : Envoyer au client son numéro d'ordre de communication
            out.println("==================================================");
            out.println("  Bienvenue sur le Serveur Multi-threads TCP !    ");
            out.println("  Vous êtes le CLIENT N°" + clientNumber);
            out.println("  Votre adresse distante : " + remoteAddress);
            out.println("==================================================");
            out.println("Entrez vos opérations (ex: 55 * 25) ou 'quitter' pour vous déconnecter.");

            String ligne;
            while ((ligne = in.readLine()) != null) {
                ligne = ligne.trim();

                if (ligne.isEmpty()) {
                    continue;
                }

                // Vérification de la commande de déconnexion
                if (ligne.equalsIgnoreCase("quitter") || ligne.equalsIgnoreCase("exit") || ligne.equals("0")) {
                    System.out.println("[Thread-" + clientNumber + "] Le Client n°" + clientNumber + " a demandé la déconnexion.");
                    out.println("Déconnexion acceptée. Au revoir Client n°" + clientNumber + " !");
                    break;
                }

                System.out.println("[Thread-" + clientNumber + " | Client n°" + clientNumber + " (" + remoteAddress + ")] Opération reçue : \"" + ligne + "\"");

                // Traitement et calcul de l'opération
                String resultat = calculerOperation(ligne);

                System.out.println("[Thread-" + clientNumber + " | Client n°" + clientNumber + "] Résultat calculé : " + resultat);

                // Renvoi de la réponse au client
                out.println(resultat);
            }

        } catch (IOException e) {
            System.err.println("[Thread-" + clientNumber + "] Déconnexion brutale ou erreur réseau avec le Client n°" 
                    + clientNumber + " : " + e.getMessage());
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("[Thread-" + clientNumber + "] Erreur lors de la fermeture du socket : " + e.getMessage());
            }
            System.out.println("[Serveur] Session terminée pour le Client n°" + clientNumber + " (" + remoteAddress + ").");
        }
    }

    /**
     * Analyse la chaîne de caractères et effectue le calcul arithmétique.
     */
    private String calculerOperation(String ligne) {
        Matcher matcher = OPERATION_PATTERN.matcher(ligne);
        if (!matcher.matches()) {
            return "Erreur : Format d'opération non reconnu ! Syntaxe attendue : <op1> <opérateur> <op2> (ex: 55 * 25)";
        }

        try {
            double op1 = Double.parseDouble(matcher.group(1));
            char operateur = matcher.group(2).charAt(0);
            double op2 = Double.parseDouble(matcher.group(3));

            double resultat;
            switch (operateur) {
                case '+':
                    resultat = op1 + op2;
                    break;
                case '-':
                    resultat = op1 - op2;
                    break;
                case '*':
                    resultat = op1 * op2;
                    break;
                case '/':
                    if (op2 == 0) {
                        return "Erreur : Division par zéro impossible !";
                    }
                    resultat = op1 / op2;
                    break;
                default:
                    return "Erreur : Opérateur '" + operateur + "' non valide (+, -, *, / attendus) !";
            }

            // Affichage élégant : entier si pas de décimales
            if (resultat == (long) resultat) {
                return String.format("%d", (long) resultat);
            } else {
                return String.valueOf(resultat);
            }

        } catch (NumberFormatException e) {
            return "Erreur : Opérandes numériques non valides !";
        }
    }
}
