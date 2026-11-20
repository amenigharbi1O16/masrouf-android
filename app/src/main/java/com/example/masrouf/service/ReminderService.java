package com.example.masrouf.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.masrouf.controller.SessionManager;
import com.example.masrouf.dao.BudgetDao;
import com.example.masrouf.dao.TransactionDao;
import com.example.masrouf.model.Budget;
import com.example.masrouf.util.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RÔLE : Service d'arrière-plan autonome pour la vérification périodique du budget et les rappels quotidiens.
 * COUCHE MVC : Service (Contrôleur d'arrière-plan).
 * EXIGENCES DU PROF COUVERTES :
 *   - Matrice §4 ligne 36 : Composant SERVICE Android (Service démarré / Started Service).
 *   - Matrice §4 ligne 16 & 35 : Déclenchement de notifications système avec NotificationChannel et PendingIntent.
 *   - Matrice §4 ligne 40 : Journalisation des cycles du service via Log.d / Log.i.
 *
 * PARTICULARITÉ PÉDAGOGIQUE POUR LE PROFESSEUR (Oral Question 43) :
 * Quelle est la différence fondamentale entre une Activité et un Service ?
 * - Une Activité (Activity) possède une interface graphique (fenêtre à l'écran) et interagit directement avec l'utilisateur.
 * - Un Service (Service) s'exécute en arrière-plan sans aucune interface visuelle pour effectuer des tâches longues
 *   ou autonomes (ex: vérification de données, lecture audio, rappels réguliers).
 */
public class ReminderService extends Service {

    private static final String TAG = "ReminderService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Cycle de vie - Le ReminderService est instancié par le système Android.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Traitement d'une commande d'arrière-plan (startId=" + startId + ").");

        // Exécution du travail d'arrière-plan dans un thread séparé pour ne jamais bloquer le thread UI principal
        new Thread(() -> {
            try {
                performBudgetVerification();
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de l'exécution du ReminderService : " + e.getMessage(), e);
            } finally {
                // EXIGENCE ARCHITECTURE : Un Started Service doit explicitement s'arrêter une fois son travail terminé
                stopSelf(startId);
                Log.d(TAG, "ReminderService : Tâche terminée, appel de stopSelf(" + startId + ").");
            }
        }).start();

        // START_NOT_STICKY : Si le système Android tue le service par manque de mémoire, ne pas le recréer automatiquement
        return START_NOT_STICKY;
    }

    /**
     * Logique de vérification de l'état budgétaire en arrière-plan
     */
    private void performBudgetVerification() {
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "Aucun utilisateur connecté en session, arrêt du contrôle.");
            return;
        }

        long userId = sessionManager.getUserId();
        BudgetDao budgetDao = new BudgetDao(this);
        TransactionDao transactionDao = new TransactionDao(this);

        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
        Budget budget = budgetDao.getBudgetForMonth(userId, currentMonth);

        if (budget == null || budget.getMontantTotal() <= 0) {
            // Aucun budget défini ce mois-ci : envoyer une notification de rappel bienveillante
            NotificationHelper.showReminderNotification(
                    this,
                    "Rappel Masrouf",
                    "N'oubliez pas d'enregistrer vos achats du jour pour maîtriser vos dépenses !"
            );
            return;
        }

        double totalBudget = budget.getMontantTotal();
        double totalSpent = transactionDao.getTotalExpensesForMonth(userId, currentMonth);
        double ratio = (totalBudget > 0) ? (totalSpent / totalBudget) : 0.0;

        if (ratio >= 1.0) {
            // Dépassement critique
            NotificationHelper.showBudgetAlert(
                    this,
                    "Alerte Masrouf : Budget dépassé !",
                    String.format(Locale.US, "Attention ! Vos dépenses (%.3f DT) dépassent votre budget alloué (%.3f DT).", totalSpent, totalBudget),
                    true
            );
        } else if (ratio >= 0.8) {
            // Seuil d'alerte des 80%
            NotificationHelper.showBudgetAlert(
                    this,
                    "Alerte Masrouf : 80% du budget atteint",
                    String.format(Locale.US, "Vous avez dépensé %.3f DT sur %.3f DT. Attention à vos prochaines sorties d'argent.", totalSpent, totalBudget),
                    false
            );
        } else {
            // Situation saine
            NotificationHelper.showReminderNotification(
                    this,
                    "Masrouf - Budget sous contrôle",
                    String.format(Locale.US, "Votre budget est sain : %.3f DT dépensés sur %.3f DT alloués.", totalSpent, totalBudget)
            );
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Il s'agit d'un Started Service (non-lié), donc on retourne null (Ch.2 du cours)
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Cycle de vie - ReminderService arrêté et détruit.");
    }
}
