package activite2.clientPackage;

import activite2.model.Operation;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Script de simulation automatisée de clients concurrents pour l'Activité 3-2.
 * Démontre l'accès concurrent synchronisé au compteur global du serveur.
 */
public class SimulateConcurrentClients {

    private static final String HOST = "localhost";
    private static final int PORT = 1234;
    private static final int NUM_CLIENTS = 3;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==========================================================");
        System.out.println("  SIMULATION DE CLIENTS CONCURRENTS (Activité 3-2)");
        System.out.println("==========================================================");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(NUM_CLIENTS);

        // Opérations réparties par client
        Operation[][] operationsPerClient = new Operation[][] {
            {
                new Operation(55, '*', 25),
                new Operation(100, '-', 45)
            },
            {
                new Operation(42, '+', 58),
                new Operation(15, '/', 4)
            },
            {
                new Operation(200, '/', 5),
                new Operation(10, '/', 0) // Test division par zéro
            }
        };

        for (int i = 0; i < NUM_CLIENTS; i++) {
            final int clientIdx = i + 1;
            final Operation[] ops = operationsPerClient[i];

            new Thread(() -> {
                try {
                    startLatch.await(); // Attente du signal de départ commun

                    InetAddress serverAddr = InetAddress.getByName(HOST);
                    InetSocketAddress socketAddr = new InetSocketAddress(serverAddr, PORT);

                    Socket socket = new Socket();
                    socket.connect(socketAddr, 5000);

                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                    // Lecture du message d'accueil
                    Object welcome = ois.readObject();
                    System.out.println("[Client Thread #" + clientIdx + "] " + welcome);

                    for (Operation op : ops) {
                        oos.writeObject(op);
                        oos.flush();
                        oos.reset();

                        Object responseObj = ois.readObject();
                        if (responseObj instanceof Operation) {
                            Operation res = (Operation) responseObj;
                            System.out.println("[Client Thread #" + clientIdx + "] Reçu -> " + res 
                                    + " (Compteur global serveur : #" + res.getOperationId() + ")");
                        }
                    }

                    // Déconnexion
                    oos.writeObject(null);
                    oos.flush();

                    ois.close();
                    oos.close();
                    socket.close();
                } catch (Exception e) {
                    System.err.println("[Client Thread #" + clientIdx + "] Erreur : " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        // Lancement simultané des clients
        System.out.println("[Simulateur] Démarrage simultané des " + NUM_CLIENTS + " clients...");
        startLatch.countDown();

        // Attente de la fin de tous les clients
        endLatch.await();
        System.out.println("\n[Simulateur] Tous les clients ont terminé avec succès leurs opérations !");
    }
}
