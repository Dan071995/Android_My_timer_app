package com.example.skillbox_project_10

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.graphics.Color
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.skillbox_project_10.databinding.ActivityMainBinding
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    //Задаем ключи переменных, которые должны сохранять свои состояния, в Bundle
    private val userDelayKey = "userDelayKey"
    private val flagKey = "flagKey"
    private val alarmFlagKey = "alarmFlagKey"
    private val progressBarProgressKey = "progressBarProgressKey"
    private val startValueKey = "startValueKey"
    private val ringtoneKey = "ringtoneKey"

    //На состоянии 3-ех следующих переменных основана вся логика программы.
    //Если Activity будет уничтожен(например при повороте экрана) то сработает метод onSaveInstanceState(outState: Bundle)
    // в котором мы сохраним ТЕКУЩЕЕ состояние 3-этих переменных.
    // При создании нового Activity сработает метод onRestoreInstanceState(savedInstanceState: Bundle), в котором мы присвоим нашим
    //3-ем глобальным переменным предидущие состояния.Тем самым при перерождении Activity мы продолжим работу программы с того места, на котором остановились!
    private var flag = false
    private var alarmFlag = true
    private var userDelay = 10

    private var ringtone: Ringtone? = null

    //Создаем экземпляр класса, который хранит рингтон
    private var ring_1: Ring? = null

    private var startValue = userDelay //Стартовое значение секундомера. Его необходимо знать чтобы корректно отображать шкалу ProgressBar-а
    private var progressBarProgress = 0

    private val coroutineJob = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    //Сохраняем текущие параметры flag/alarmFlag/userDelay при уничтожении Activity
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(flagKey,flag)
        outState.putBoolean(alarmFlagKey,alarmFlag)
        outState.putInt(userDelayKey,userDelay)
        outState.putInt(progressBarProgressKey,progressBarProgress)
        outState.putInt(startValueKey,startValue)
        outState.putParcelable(ringtoneKey,ring_1)
    }

    //Востанавливаем значения  flag/alarmFlag/userDelay при перезапуске Activity

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        flag = savedInstanceState.getBoolean(flagKey)
        alarmFlag = savedInstanceState.getBoolean(alarmFlagKey)
        userDelay = savedInstanceState.getInt(userDelayKey)
        progressBarProgress = savedInstanceState.getInt(progressBarProgressKey)
        startValue = savedInstanceState.getInt(startValueKey)
        //получаем экземпляр класса com.example.skillbox_project_10.Ring, хранящий рингтон,
        //который был создан при первом запуске программы
        ring_1 = savedInstanceState.getParcelable(ringtoneKey)
        //Получаем первоначальный рингтон из класса com.example.skillbox_project_10.Ring.
        ringtone = ring_1!!.getRingtone()

        binding.timerTextView.text = userDelay.toString()
        binding.progressBar.max = startValue
        binding.progressBar.progress = progressBarProgress

        if(!flag) binding.startStopButton.text = "Start!" else binding.startStopButton.text = "Stop!"

        if (userDelay <= 0 && flag) {
            alarmFlag = false
            binding.startStopButton.setBackgroundColor(Color.RED)
            binding.timerTextView.setTextColor(Color.RED)
        }
    }

    //Функция сброса прогресса
    private fun refreshProgress(){
        binding.timerTextView.text = userDelay.toString()
        binding.progressBar.max = userDelay
        progressBarProgress = 0
        binding.progressBar.progress = progressBarProgress
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()

        //Создаем рингтон (новый рингтон создается при каждом запуске MainActivity)
        val notification:Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL)
        val ringt = RingtoneManager.getRingtone(this, notification)
        //Если переменная, которая должна хранить экземпляр класса com.example.skillbox_project_10.Ring = null, создаем экземпляр класса
        //com.example.skillbox_project_10.Ring(ringt) в который передаем вышесозданный рингтон. Теперь в функциях onRestoreInstanceState
        // и onRestoreInstanceState мы будем сохранять и востанавливать в ring_1 экземпляр com.example.skillbox_project_10.Ring(). Таким образом
        //при 2-ом запуске Activity, ring_1 будет содержать экземпляр com.example.skillbox_project_10.Ring() из первой Activity. И мы уже не
        //попадем в этот блок if()
        if (ring_1 == null) {
            ring_1 = Ring(ringt)
            ringtone = ring_1!!.getRingtone()
        }

        //Задаем значения в элементах View при построении Activity
        binding.progressBar.max = startValue
        binding.progressBar.progress = progressBarProgress
        binding.timerTextView.text = userDelay.toString()

        binding.startStopButton.setOnClickListener {
            if (!flag){
                binding.startStopButton.text = "Stop!"
                binding.editTextTime.text.clear() //очищаем поле пользовательского ввода задержки
                binding.editTextTime.isEnabled = false
                binding.seekBar.isEnabled = false
                flag = true
            }
            else {
                binding.startStopButton.text = "Start!"
                binding.editTextTime.isEnabled = true
                binding.seekBar.isEnabled = true
                binding.startStopButton.setBackgroundColor(Color.WHITE)
                binding.timerTextView.setTextColor(Color.BLACK)
                alarmFlag = true
                ringtone?.stop()
                flag = false
            }
        }

        //Текстовое поля для пользовательского ввода значения таймера
        binding.editTextTime.doOnTextChanged { _, _, _, _ ->
            val str = binding.editTextTime.text?.toString() ?: "10"
            if (str.isNotEmpty()){ //данной строчкой проверяем, что значение в поле editTextTime.text не пустое
                //Кргда активно поле GetText получаем ввденные данные и преобразуем их в Int
                userDelay = Integer.parseInt(str)
                refreshProgress()
            }
            startValue = userDelay
            progressBarProgress = 0
            binding.seekBar.progress = progressBarProgress
        }

        //Seek Bar для быстрого задания предустановленного значения таймера
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            //Данный метод считывает и возвращает значение позунка, на котором остановился пользователь
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                when(progress){
                    0 -> userDelay = 10
                    1 -> userDelay = 20
                    2 -> userDelay = 30
                    3 -> userDelay = 40
                    4 -> userDelay = 50
                    5 -> userDelay = 60
                }
                }
                refreshProgress()
                startValue = userDelay
                binding.editTextTime.text.clear()
            }
            //данный метод запоминвет стартовое значение ползунка при его активации пользователем
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            //данный метод запоминвет конечное значение ползунка при его активации пользователем
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        CoroutineScope(coroutineJob + Dispatchers.Main).launch {

            while (currentCoroutineContext().isActive){
                if (flag && userDelay > 0 && alarmFlag){
                userDelay -= 1
                progressBarProgress += 1
                binding.progressBar.progress = progressBarProgress
                binding.timerTextView.text = userDelay.toString()
                }
            if (userDelay <= 0 && flag && alarmFlag) {
                alarmFlag = false
                binding.startStopButton.setBackgroundColor(Color.RED)
                binding.timerTextView.setTextColor(Color.RED)
                ringtone?.play()
                Toast.makeText(this@MainActivity,"Time is over!",Toast.LENGTH_LONG).show()
            }
            delay(1000L)
            }
        }

    }
    //Отменяем корутину при остановке View
    override fun onStop() {
        super.onStop()
        Log.d(TAG,"onStop")
        coroutineJob.complete()
        coroutineJob.cancel()
        //ringtone?.stop()
    }

}