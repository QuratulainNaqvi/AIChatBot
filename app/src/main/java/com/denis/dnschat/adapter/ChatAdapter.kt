package com.denis.dnschat.adapter// ChatAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.denis.dnschat.R
import com.denis.dnschat.interfaces.ChatVoiceClick
import com.denis.dnschat.model.ChatMessage

class ChatAdapter(
    private val messages: MutableList<ChatMessage>,
    private val chatClick: ChatVoiceClick
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isChatBotSpeaking: Boolean = false
    var isUserSpeaking: Boolean = false

    companion object {
        const val VIEW_TYPE_USER = 1
        const val VIEW_TYPE_CHATBOT = 2
        const val VIEW_TYPE_SKELETON = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> {
                UserViewHolder(inflater.inflate(R.layout.user_message_item, parent, false))
            }

            VIEW_TYPE_CHATBOT -> {
                ChatbotViewHolder(inflater.inflate(R.layout.ai_message_item, parent, false))
            }

            else -> {
                SkeletonViewHolder(inflater.inflate(R.layout.loading_effect, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder.itemViewType) {
            VIEW_TYPE_USER -> (holder as UserViewHolder).bind(message, chatClick)
            VIEW_TYPE_CHATBOT -> (holder as ChatbotViewHolder).bind(message, chatClick)
            VIEW_TYPE_SKELETON -> (holder as SkeletonViewHolder)
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].sourceType) {
            "USER" -> {
                VIEW_TYPE_USER
            }

            "AI" -> {
                VIEW_TYPE_CHATBOT
            }

            else -> {
                VIEW_TYPE_SKELETON
            }
        }
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.userMessageTextView)
        private val speakIcon: ImageView = itemView.findViewById(R.id.iv_speak)
        private val stopIcon: ImageView = itemView.findViewById(R.id.iv_stop)

        fun bind(message: ChatMessage, clickChat: ChatVoiceClick) {
            messageTextView.text = message.message
            speakIcon.setOnClickListener {
                isUserSpeaking = true
                speakIcon.visibility = View.GONE
                stopIcon.visibility = View.VISIBLE
                clickChat.onChatClicked(it, message, adapterPosition, "userChat")
            }
            stopIcon.setOnClickListener {
                isUserSpeaking = false
                speakIcon.visibility = View.VISIBLE
                stopIcon.visibility = View.GONE
                clickChat.onStopSpeech(it, message, adapterPosition, "userChat")
            }


            if (!isUserSpeaking) {
                speakIcon.visibility = View.VISIBLE
                stopIcon.visibility = View.GONE
            }


        }
    }

    inner class ChatbotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.aiMessageTextView)
        private val speakIcon: ImageView = itemView.findViewById(R.id.iv_speak)
        private val stopIcon: ImageView = itemView.findViewById(R.id.iv_stop)


        fun bind(message: ChatMessage, clickChat: ChatVoiceClick) {
            messageTextView.text = message.message
            speakIcon.setOnClickListener {
                speakIcon.visibility = View.GONE
                stopIcon.visibility = View.VISIBLE
                clickChat.onChatClicked(it, message, adapterPosition, "aiChat")
                isChatBotSpeaking = true
            }
            stopIcon.setOnClickListener {
                speakIcon.visibility = View.VISIBLE
                stopIcon.visibility = View.GONE
                clickChat.onStopSpeech(it, message, adapterPosition, "aiChat")
                isChatBotSpeaking = false
            }

            if (!isChatBotSpeaking) {
                speakIcon.visibility = View.VISIBLE
                stopIcon.visibility = View.GONE
            }
        }
    }

    inner class SkeletonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun removeMessage(position: Int) {
        if (position in 0 until messages.size) {
            messages.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun stopAISpeech() {
        if (isChatBotSpeaking) {
            isChatBotSpeaking = false
            if (isUserSpeaking) {
                isUserSpeaking = false
            }
            notifyDataSetChanged()
        }
    }

    fun stopUserSpeech() {
        if (isUserSpeaking) {
            isUserSpeaking = false

            if (isChatBotSpeaking) {
                isChatBotSpeaking = false
            }
            notifyDataSetChanged()
        }
    }
}
