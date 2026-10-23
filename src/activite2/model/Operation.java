package activite2.model;

import java.io.Serializable;

/**
 * TP 3 - Activité 3-2 : Serveur Multi-threads TCP (Calculatrice par Objets)
 * 
 * Classe Operation :
 * - Implémente l'interface java.io.Serializable pour permettre la transmission
 *   sérialisée d'objets via ObjectInputStream et ObjectOutputStream.
 * - Encapsule les opérandes (op1, op2), l'opérateur (+, -, *, /),
 *   le résultat du calcul, le statut d'erreur et le numéro d'ordre du client/opération.
 */
public class Operation implements Serializable {

    private static final long serialVersionUID = 1L;

    private double op1;
    private double op2;
    private char operateur;
    private double resultat;
    private boolean isErreur;
    private String messageErreur;
    private int clientNumber;
    private int operationId;

    /**
     * Constructeur pour initialiser les arguments nécessaires au calcul.
     * 
     * @param op1 Premier opérande
     * @param operateur Opérateur arithmétique (+, -, *, /)
     * @param op2 Deuxième opérande
     */
    public Operation(double op1, char operateur, double op2) {
        this.op1 = op1;
        this.operateur = operateur;
        this.op2 = op2;
        this.isErreur = false;
        this.messageErreur = null;
    }

    // Constructeur étendu avec identifiant client
    public Operation(double op1, char operateur, double op2, int clientNumber) {
        this(op1, operateur, op2);
        this.clientNumber = clientNumber;
    }

    // Getters et Setters
    public double getOp1() {
        return op1;
    }

    public void setOp1(double op1) {
        this.op1 = op1;
    }

    public double getOp2() {
        return op2;
    }

    public void setOp2(double op2) {
        this.op2 = op2;
    }

    public char getOperateur() {
        return operateur;
    }

    public void setOperateur(char operateur) {
        this.operateur = operateur;
    }

    public double getResultat() {
        return resultat;
    }

    public void setResultat(double resultat) {
        this.resultat = resultat;
        this.isErreur = false;
    }

    public boolean isErreur() {
        return isErreur;
    }

    public void setErreur(boolean isErreur) {
        this.isErreur = isErreur;
    }

    public String getMessageErreur() {
        return messageErreur;
    }

    public void setMessageErreur(String messageErreur) {
        this.messageErreur = messageErreur;
        this.isErreur = true;
    }

    public int getClientNumber() {
        return clientNumber;
    }

    public void setClientNumber(int clientNumber) {
        this.clientNumber = clientNumber;
    }

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    @Override
    public String toString() {
        if (isErreur) {
            return "Opération [" + formatDouble(op1) + " " + operateur + " " + formatDouble(op2) + "] -> " + messageErreur;
        }
        return formatDouble(op1) + " " + operateur + " " + formatDouble(op2) + " = " + formatDouble(resultat);
    }

    private String formatDouble(double val) {
        if (val == (long) val) {
            return String.format("%d", (long) val);
        }
        return String.valueOf(val);
    }
}
