package com.deleon.skynet.card

import android.content.Context
import android.icu.text.DecimalFormat
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.deleon.skynet.R

class CardHelper(
    private val context: Context,
    private val holder: ViewGroup
) {
    private var _allCards: Array<CardModel> = arrayOf()

    /**
     * Refresh.
     *
     * @param cards [Array]<[CardModel]>
     * @param onDeleteListener [OnButtonListener.onDelete]
     * @param onPrintListener [OnButtonListener.onPrint]
     * @param onSaveListener [OnButtonListener.onSave]
     */
    fun refresh(
        cards: Array<CardModel>,
        onDeleteListener: ((model: CardModel) -> Unit)? = null,
        onPrintListener: ((model: CardModel, server: String, label: String) -> Unit)? = null,
        onSaveListener: ((newPrice: Double, targetProfile: String) -> Unit)? = null
    ) {
        refresh(cards, object : OnButtonListener {
            override fun onSave(price: Double, profile: String) {
                if (onSaveListener != null) onSaveListener(price, profile)
            }

            override fun onPrint(cardModel: CardModel, server: String, label: String) {
                if (onPrintListener != null) onPrintListener(cardModel, server, label)
            }

            override fun onDelete(cardModel: CardModel) {
                if (onDeleteListener != null) onDeleteListener(cardModel)
            }
        })
    }

    private fun refresh(cards: Array<CardModel>, listener: OnButtonListener) {
        _allCards = cards
        holder.removeAllViews()
        _allCards.forEach { model ->
            val view: View = View.inflate(context, R.layout.item_voucher, null)
            val itemPreview: ConstraintLayout = view.findViewById(R.id.item_voucher_view)
            val editorView: RelativeLayout = view.findViewById(R.id.item_editor_view)
            val username: TextView = itemPreview.findViewById(R.id.item_voucher_username)
            val profile: TextView = itemPreview.findViewById(R.id.item_voucher_profile)
            val price: TextView = itemPreview.findViewById(R.id.item_voucher_price)
            val uptime: TextView = itemPreview.findViewById(R.id.item_voucher_uptime)
            val priceEdit: EditText = editorView.findViewById(R.id.price_editor_price)
            val saveButton: Button = editorView.findViewById(R.id.item_save_button)
            val deleteButton: Button = editorView.findViewById(R.id.item_delete_button)
            val printButton: Button = editorView.findViewById(R.id.item_print_button)
            view.id = View.generateViewId()
            username.text = model.username
            profile.text = model.profile
            uptime.text = formatUptime(model.timeLimit)
            price.text = formatPrice(model.price)
            priceEdit.setText(model.price.toString())
            saveButton.setOnClickListener { listener.onSave(priceEdit.text.toString().toDouble(), model.profile) }
            printButton.setOnClickListener { listener.onPrint(model, "http://sky.net", "Skynet") }
            deleteButton.setOnClickListener { listener.onDelete(model) }
            editorView.visibility = View.GONE
            itemPreview.setOnClickListener {
                itemPreview.visibility = View.GONE
                editorView.visibility = View.VISIBLE
                editorView.findViewById<TextView>(R.id.price_editor_label).performClick()
            }
            holder.addView(view)
        }
    }

    override fun toString(): String {
        val mLines = mutableListOf(DEFAULT_STRING)
        _allCards.forEach {
            mLines.add(it.asString())
        }
        return mLines.joinToString("\n")
    }

    /** On Button Listener. */
    interface OnButtonListener {
        /**
         * On Save.
         *
         * @param price [Double]
         * @param profile [String]
         */
        fun onSave(price: Double, profile: String)

        /**
         * On Print.
         *
         * @param cardModel [CardModel]
         * @param server [String] server url
         * @param label [String] server name
         */
        fun onPrint(cardModel: CardModel, server: String, label: String)

        /**
         * On Delete.
         *
         * @param cardModel [CardModel]
         */
        fun onDelete(cardModel: CardModel)
    }

    companion object {
        /** Default String */
        @JvmStatic
        val DEFAULT_STRING = "Username,Password,Profile,Time Limit,Data Limit,Comment"

        /**
         * As String.
         *
         * @return [String]
         * @receiver [CardModel]
         */
        @JvmStatic
        fun CardModel.asString(): String {
            return "$username,$password,$profile,$timeLimit,$dataLimit,$comment"
        }

        /**
         * Time Limit Of.
         *
         * @param source
         * @return [String]
         */
        @JvmStatic
        fun timeLimitOf(source: String): String {
            if (source.trim().isEmpty()) return ""
            val regex = Regex("(([0-9]{1,2}+[dhmwDHMW])+)", RegexOption.IGNORE_CASE)
            return regex.find(source.trim(), 0)?.value ?: ""
        }

        /**
         * Price Amounts Of.
         *
         * @param profile
         * @return [Double]
         */
        @JvmStatic
        fun priceAmountsOf(profile: String): Double {
            if (profile.trim().isEmpty()) return 0.0
            return when (profile.trim().lowercase()) {
                "2k" -> 2000.0
                "5k" -> 5000.0
                "15k" -> 15000.0
                "50k" -> 50000.0
                else -> 0.0
            }
        }

        /**
         * Format Uptime.
         *
         * @param timeLimit
         * @return [String]
         */
        @JvmStatic
        fun formatUptime(timeLimit: String): String {
            if (timeLimit.isEmpty()) return "Lifetime"
            return timeLimit.lowercase().replace("m", " Bulan ")
                .replace("h", " Jam ").replace("d", " Hari ")
                .replace("w", " Minggu ").trim()
        }

        /**
         * Format Price.
         *
         * @param amounts
         * @param pattern
         * @return [String]
         */
        @JvmOverloads
        @JvmStatic
        fun formatPrice(amounts: Double, pattern: String = "Rp #,##0.00"): String {
            return DecimalFormat(pattern).format(amounts)
        }

        /**
         * Create Card.
         *
         * @param source
         * @return [CardModel] or null
         */
        @JvmStatic
        fun createCard(source: String): CardModel? {
            if (source.trim().isEmpty() || source.trim().equals(DEFAULT_STRING, true)) return null
            val str = source.split(",")
            if (str[0].trim().isEmpty()) return null
            val username = str[0].trim()
            val password = if (str.size > 1) str[1].trim() else username
            val profile = if (str.size > 2 && str[2].trim().isNotEmpty()) str[2].trim() else "default"
            val timeLimit = if (str.size > 3) timeLimitOf(str[3]) else ""
            val dataLimit = if (str.size > 4) str[4].trim() else ""
            val comment = if (str.size > 5) str[5].trim() else ""
            val price = priceAmountsOf(profile)
            return CardModel(username, password, profile, timeLimit, dataLimit, comment, price)
        }

        /**
         * As Text.
         *
         * @param server [String] server url
         * @param label [String] server name
         * @return [String]
         * @receiver [CardModel]
         */
        @JvmStatic
        fun CardModel.asText(server: String = "http://sky.net", label: String = "Skynet"): String {
            val lines = mutableListOf("[C]<font size='big'>$label</font>")
            lines.add("[L]")
            lines.add("[C]<qrcode size='30'>$server/login?username=${username}&password=${password}</qrcode>")
            lines.add("[L]")
            lines.add("[C]<font size='big'>${formatUptime(timeLimit)}</font>")
            lines.add("[L]")
            if (username == password) {
                lines.add("[C]Kode Voucher :")
                lines.add("[C]<font size='big'>${username}</font>")
            } else {
                lines.add("[C]Username :")
                lines.add("[C]<font size='big'>${username}</font>")
                lines.add("[C]Password :")
                lines.add("[C]<font size='big'>${password}</font>")
            }
            lines.add("[C]Login: $server")
            lines.add("[L]")
            lines.add("[C]<font size='big'>${formatPrice(price)}</font>")
            lines.add("[L]")
            return lines.joinToString("\n")
        }
    }
}