package com.steal.voiceassistant.model

/**
 * Identifiants des éléments d'interface WhatsApp utilisés par
 * l'AccessibilityService pour automatiser l'envoi d'un message.
 *
 * ATTENTION : ces resource-id peuvent changer à chaque mise à jour de
 * WhatsApp. Pour les retrouver après une mise à jour :
 *   1. Ouvrir WhatsApp sur le téléphone
 *   2. Lancer `uiautomatorviewer` (fourni avec Android Studio / Android SDK)
 *   3. Cliquer sur l'élément (ex: le bouton recherche) pour voir son "resource-id"
 *   4. Mettre à jour les constantes ci-dessous
 */
object WhatsAppProfile {
    const val PACKAGE_NAME = "com.whatsapp"

    // Icône de recherche sur l'écran des conversations
    const val SEARCH_BUTTON_ID = "com.whatsapp:id/menuitem_search"
    // Champ de saisie de la recherche
    const val SEARCH_INPUT_ID = "com.whatsapp:id/search_src_text"
    // Premier résultat de conversation dans la liste
    const val CONVERSATION_ROW_ID = "com.whatsapp:id/contact_row_container"
    // Champ de saisie du message dans une conversation ouverte
    const val MESSAGE_INPUT_ID = "com.whatsapp:id/entry"
    // Bouton d'envoi
    const val SEND_BUTTON_ID = "com.whatsapp:id/send"
}
