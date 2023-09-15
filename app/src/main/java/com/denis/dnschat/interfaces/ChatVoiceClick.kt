package com.denis.dnschat.interfaces

import android.view.View
import com.denis.dnschat.model.ChatMessage

interface ChatVoiceClick {
    fun onChatClicked(view: View?,chatMessage: ChatMessage,position: Int,chatType:String)
    fun onStopSpeech(view: View?,chatMessage: ChatMessage,position: Int,chatType:String)
}