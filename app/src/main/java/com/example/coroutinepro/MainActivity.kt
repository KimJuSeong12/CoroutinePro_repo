package com.example.coroutinepro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.example.coroutinepro.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import java.util.Objects
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var handler: Handler
    lateinit var channel: Channel<Long>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 1. 핸들러 등록
        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                binding.tvSumResult.text = "sum = ${msg.obj}"
            }
        }

        // 2.1 Message 역할 (코루틴 처리방식 채널객체생성)
        channel = Channel<Long>()
        // 2.2 Handler 역할 (코루틴 처리방식 채널객체생성)
        val mainScope = GlobalScope.launch(Dispatchers.Main) {
            channel.consumeEach {
                binding.tvSumResult.text = "sum = ${it}"
            }
        }

        // 3. 스레드를 설계한다.
        val backgroundScope = CoroutineScope(Dispatchers.Default + Job())

        // 오랫동안 시간이 걸리는 작업을(6~8) 이벤트 요청
        binding.btnClick.setOnClickListener {
            // backgroundScope.launch ( 1번 방식으로 하려면 thread로 바꿔쓰면 된다.)
            backgroundScope.launch {
                var sum = 0L
                var time = measureTimeMillis {
                    for (i in 1..2_000_000_000) {
                        sum += i
                    }
                }
                Log.e("MainActivity", "${time}")

                // ++++++++++++++++++++++++++++++ 1번방식으로 할 때 ++++++++++++
//                val message:Message = Message()
//                message.obj = "${sum}"
//                handler.sendMessage(message)
                // ++++++++++++++++++++++++++++++ 2번방식 코루틴으로 할 때 ++++++++++++
                channel.send(sum.toLong())
            }
        }
        binding.btnClean.setOnClickListener {
            binding.tvSumResult.text = "합계출력: 0"
        }
    }
}