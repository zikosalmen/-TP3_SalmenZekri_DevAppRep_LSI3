package activite2.serverPackage;

import activite2.model.Operation;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * TP 3 - Activité 3-2 : Serveur Multi-threads TCP (Calculatrice par Objets)
 * 
 * Classe ClientProcess :
 * - Thread dédié au traitement d'un client spécifique par échange d'objets sérialisés
 * - Manipule les flux ObjectOutputStream et ObjectInputStream
 * - Respecte la règle d'initialisation des flux d'objets (OOS en premier + flush)
 * - Traite les requêtes arithmétiques encapsulées dans des instances d'Operation
 * - Met à jour le compteur global partagé synchronisé du serveur
 * - Renvoie l'objet Operation complété avec le résultat ou l'erreur
 */
public class ClientProcess extends Thread {

    private final Socket socket;
    private final int clientNumber;
    private final SocketAddress remoteAddress;

    public ClientProcess(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        this.remoteAddress = socket.getRemoteSocketAddress();
    }

    @Override
    public void run() {
        System.out.println("[Thread-" + clientNumber + "] Démarrage de la session pour Client n°" 
                + clientNumber + " (" + remoteAddress + ")");

        try {
            // RÈGLE D'OR : Instancier ObjectOutputStream en premier et vider le tampon (flush)
            // afin d'émettre l'en-tête de sérialisation et d'éviter tout interblocage (Deadlock).
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            // Envoi au client de son numéro d'ordre sous forme d'objet String d'accueil
            oos.writeObject("Bienvenue ! Vous êtes le CLIENT N°" + clientNumber + " (Adresse : " + remoteAddress + ")");
            oos.flush();
            oos.reset();

            while (true) {
                try {
                    // Réception de l'objet émis par le client
                    Object obj = ois.readObject();

                    // Vérification du signal de déconnexion volontaire
                    if (obj == null) {
                        System.out.println("[Thread-" + clientNumber + "] Signal de déconnexion reçu du Client n°" + clientNumber);
                        break;
                    }

                    if (obj instanceof Operation) {
                        Operation op = (Operation) obj;
                        op.setClientNumber(clientNumber);

                        double op1 = op.getOp1();
                        char operateur = op.getOperateur();
                        double op2 = op.getOp2();

                        System.out.println("\n[Thread-" + clientNumber + " | Client n°" + clientNumber + "] Opération reçue : " 
                                + op1 + " " + operateur + " " + op2);

                        // Calcul de l'opération
                        switch (operateur) {
                            case '+':
                                op.setResultat(op1 + op2);
                                break;
                            case '-':
                                op.setResultat(op1 - op2);
                                break;
                            case '*':
                                op.setResultat(op1 * op2);
                                break;
                            case '/':
                                if (op2 == 0) {
                                    op.setMessageErreur("Erreur : Division par zéro impossible !");
                                } else {
                                    op.setResultat(op1 / op2);
                                }
                                break;
                            default:
                                op.setMessageErreur("Erreur : Opérateur '" + operateur + "' non supporté !");
                                break;
                        }

                        // ÉTAPE 3 : GESTION DE L'ACCÈS CONCURRENT
                        // Incrémentation et affichage du compteur global partagé synchronisé
                        String desc = op1 + " " + operateur + " " + op2 + " = " + (op.isErreur() ? op.getMessageErreur() : op.getResultat());
                        int opIndex = Server.incrementerCompteurGlobal(clientNumber, desc);
                        op.setOperationId(opIndex);

                        // Renvoi de l'objet calculé au client
                        oos.writeObject(op);
                        oos.flush();
                        oos.reset(); // Crucial pour purger le cache de références internes du flux
                        System.out.println("[Thread-" + clientNumber + "] Objet renvoyé au Client n°" + clientNumber 
                                + " (Compteur global : " + opIndex + ")");
                    } else {
                        System.err.println("[Thread-" + clientNumber + "] Objet inattendu reçu : " + obj.getClass().getName());
                    }

                } catch (EOFException | SocketException e) {
                    System.out.println("[Thread-" + clientNumber + "] Le Client n°" + clientNumber + " s'est déconnecté.");
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("[Thread-" + clientNumber + "] Type d'objet inconnu : " + e.getMessage());
                    break;
                }
            }

            // Fermeture des flux
            ois.close();
            oos.close();

        } catch (IOException e) {
            System.err.println("[Thread-" + clientNumber + "] Erreur de communication : " + e.getMessage());
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("[Thread-" + clientNumber + "] Erreur fermeture socket : " + e.getMessage());
            }
            System.out.println("[Serveur] Session terminée pour Client n°" + clientNumber + " (" + remoteAddress + ").");
        }
    }
}
