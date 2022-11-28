package com.lootexe.foreground.model

data class NotificationConfiguration(val channel: ChannelConfiguration,
                                     val icon: IconConfiguration?,
                                     val title: String,
                                     val text: String,
                                     val visibility: Int)