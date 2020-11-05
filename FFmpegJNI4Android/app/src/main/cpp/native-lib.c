#include <jni.h>
#include <string.h>
#include <malloc.h>
#include "libavcodec/jni.h"

#include "ffmpeg.h"


int ffmpeg_run_test(int argc, char **argv) {

//    LOGE("ffmpeg_run", "argc = %d", argc);
//    for (int i = 0; i < argc; ++i) {
//        LOGE("ffmpeg_run", "argv[%d] = %s", i, argv[i]);
//    }
    return 0;
}

JNIEXPORT jint JNICALL Java_com_alan_ffmpegjni4android_MainActivity_exeFFmpegCommand(
        JNIEnv *env, jobject obj, jstring js_cmd) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());

    JavaVM * jvm;
    (*env)->GetJavaVM(env, &jvm);
    av_jni_set_java_vm(jvm, NULL);

    jboolean is_copy = 0;
    const char* c_command = (char*)(*env)->GetStringUTFChars(env, js_cmd, &is_copy);
    int c_command_len = strlen(c_command);

    int argc = 0;
    char* argv[200];// 命令参数个数 200
    char temp_arg[500]; // 每个参数最长字符数 500
    int char_count = 0;

    for(int i = 0; i <= c_command_len; i++){
        //将当前空格或终止符与前一个空格之间的字符串存放到 argv[argc] 中
        if(c_command[i] == ' ' || c_command[i] == '\0'){
            // 为当前参数分配内存，长度为：字符个数 char_count + 终止符
            argv[argc] = (char*)calloc(char_count + 1, sizeof(char));
            memcpy(argv[argc], temp_arg, char_count);
            strcat(argv[argc], "\0");

            argc++;
            char_count = 0;
            i++;
        }

        temp_arg[char_count] = c_command[i];
        char_count++;
    }

    // 执行 ffmpeg 命令
    int ret = ffmpeg_run_test(argc, argv);
    ret = ffmpeg_run(argc, argv);

    // 释放内存
    for(int i = 0; i < argc; i++){
        free(argv[i]);
        argv[i] = NULL;
    }

    if(c_command != NULL) {
        (*env)->ReleaseStringUTFChars(env, js_cmd, c_command);
    }

    return ret;
}
