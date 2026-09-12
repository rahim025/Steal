package com.steal.voiceassistant.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.steal.voiceassistant.model.WhatsAppProfile

/**
 * Service central : c'est lui qui "voit" ce qui est affiché à l'écran
 * (via onAccessibilityEvent / rootInActiveWindow) et qui peut simuler des
 * clics, du scroll et de la saisie de texte dans N'IMPORTE QUELLE app,
 * y compris quand l'écran est verrouillé (dans la limite des restrictions
 * de sécurité Android — voir README pour le détail des limites).
 */
class UnlockAccessibilityService : AccessibilityService() {

    companion object {
        // Référence statique simple pour que le reste de l'app puisse
        // appeler ce service tant qu'il est actif. À remplacer par un
        // bus d'événements/binder si le projet grossit.
        var instance: UnlockAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // On pourra ici détecter par ex. l'écran de verrouillage actif
        // via event.packageName == "com.android.systemui" pour déclencher
        // la tentative de déverrouillage vocal.
    }

    override fun onInterrupt() {}

    // ---- Actions génériques sur l'écran ----

    fun performGoHome() = performGlobalAction(GLOBAL_ACTION_HOME)
    fun performGoBack() = performGlobalAction(GLOBAL_ACTION_BACK)
    fun openNotifications() = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)

    /** Tente le déverrouillage (fonctionne seulement sans code/schéma actif). */
    fun attemptUnlock() {
        performGlobalAction(GLOBAL_ACTION_HOME)
        // Swipe du bas vers le haut au centre de l'écran, geste standard
        // pour révéler l'écran d'accueil sur un verrouillage "glissement simple".
        val displayMetrics = resources.displayMetrics
        val path = Path().apply {
            moveTo(displayMetrics.widthPixels / 2f, displayMetrics.heightPixels * 0.8f)
            lineTo(displayMetrics.widthPixels / 2f, displayMetrics.heightPixels * 0.2f)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()
        dispatchGesture(gesture, null, null)
    }

    /** Cherche un noeud dont le texte ou la description contient `label` et clique dessus. */
    fun clickByLabel(label: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val node = findNodeByText(root, label) ?: return false
        return clickNode(node)
    }

    fun clickByResourceId(resourceId: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByViewId(resourceId)
        val node = nodes.firstOrNull() ?: return false
        return clickNode(node)
    }

    fun typeTextInField(resourceId: String, text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByViewId(resourceId)
        val node = nodes.firstOrNull() ?: return false
        val arguments = Bundle()
        arguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text
        )
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    private fun clickNode(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            current = current.parent
        }
        return false
    }

    private fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val matches = root.findAccessibilityNodeInfosByText(text)
        return matches.firstOrNull()
    }

    /**
     * Enchaîne les étapes pour envoyer un message WhatsApp.
     * Simplifié : dans un vrai enchaînement, chaque étape doit attendre
     * que l'écran suivant soit chargé (ex: écouter onAccessibilityEvent)
     * avant de continuer. Ici on illustre la logique, à raffiner avec des
     * callbacks/coroutines et des délais/vérifications d'état.
     */
    fun sendWhatsAppMessage(contact: String, message: String) {
        clickByResourceId(WhatsAppProfile.SEARCH_BUTTON_ID)
        typeTextInField(WhatsAppProfile.SEARCH_INPUT_ID, contact)
        clickByResourceId(WhatsAppProfile.CONVERSATION_ROW_ID)
        typeTextInField(WhatsAppProfile.MESSAGE_INPUT_ID, message)
        clickByResourceId(WhatsAppProfile.SEND_BUTTON_ID)
    }

    /** Lit à voix haute (à connecter au TTS) le contenu textuel visible à l'écran. */
    fun collectScreenText(): String {
        val root = rootInActiveWindow ?: return ""
        val builder = StringBuilder()
        collectText(root, builder)
        return builder.toString().trim()
    }

    private fun collectText(node: AccessibilityNodeInfo, builder: StringBuilder) {
        node.text?.let { builder.append(it).append(". ") }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectText(it, builder) }
        }
    }
}
