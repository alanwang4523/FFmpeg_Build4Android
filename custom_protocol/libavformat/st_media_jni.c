
#include <jni.h>

#include "libavutil/avassert.h"
#include "libavutil/mem.h"
#include "libavutil/avstring.h"
#include "libavutil/internal.h"
#include "libavutil/opt.h"
#include "avformat.h"
#include "url.h"

#include "libavcodec/ffjni.h"

/**
 * Author: AlanWang4523.
 * Date: 2020/11/02 17:50.
 * Mail: alanwang4523@gmail.com
 */

struct JNIStreamProtocolFields {
     jclass class_streamprotocol;
     jmethodID jmd_init;
     jmethodID jmd_open;
     jmethodID jmd_read;
     jmethodID jmd_seek;
     jmethodID jmd_close;
};

typedef struct _STMediaContext {
    const AVClass *class;
    struct JNIStreamProtocolFields jfields;
    jobject obj_stream_protocol;
    jbyteArray jbuffer;
    int jbuffer_capacity;
} STMediaContext;

static const AVOption options[] = {
    { NULL }
};

static const AVClass stmedia_context_class = {
    .class_name = "STMedia",
    .item_name  = av_default_item_name,
    .option     = options,
    .version    = LIBAVUTIL_VERSION_INT,
};

#if CONFIG_STMEDIA_PROTOCOL

#define ST_STREAM_PROTOCOL_PATH "com/alan/ffmpegtool4android/utils/STStreamProtocol"

static const struct FFJniField jni_stream_protocol_mapping[] = {
    { ST_STREAM_PROTOCOL_PATH, NULL, NULL, FF_JNI_CLASS, offsetof(struct JNIStreamProtocolFields, class_streamprotocol), 1 },
    { ST_STREAM_PROTOCOL_PATH, "<init>", "()V", FF_JNI_METHOD, offsetof(struct JNIStreamProtocolFields, jmd_init), 1 },
    { ST_STREAM_PROTOCOL_PATH, "open", "(Ljava/lang/String;)V", FF_JNI_METHOD, offsetof(struct JNIStreamProtocolFields, jmd_open), 1 },
    { ST_STREAM_PROTOCOL_PATH, "read", "([BII)I", FF_JNI_METHOD, offsetof(struct JNIStreamProtocolFields, jmd_read), 1 },
    { ST_STREAM_PROTOCOL_PATH, "seek", "(J)I", FF_JNI_METHOD, offsetof(struct JNIStreamProtocolFields, jmd_seek), 1 },
    { ST_STREAM_PROTOCOL_PATH, "colse", "(Ljava/lang/String;)V", FF_JNI_METHOD, offsetof(struct JNIStreamProtocolFields, jmd_close), 1 },
    { NULL }
};

static int stmedia_open(URLContext *h, const char *filename, int flags)
{
    STMediaContext *context = h->priv_data;

    av_strstart(filename, "stmedia:", &filename);

    int ret = -1;
    JNIEnv *env = NULL;
    jobject object = NULL;
    jstring file_uri = NULL;

    env = ff_jni_get_env(context);
    if (!env) {
        goto exit;
    }

    if (ff_jni_init_jfields(env, &context->jfields, jni_stream_protocol_mapping, 1, context) < 0) {
        goto exit;
    }

    object = (*env)->NewObject(env, context->jfields.class_streamprotocol, context->jfields.jmd_init);
    if (!object) {
        goto exit;
    }
    context->obj_stream_protocol = (*env)->NewGlobalRef(env, object);
    if (!context->obj_stream_protocol) {
        goto exit;
    }

    file_uri = ff_jni_utf_chars_to_jstring(env, filename, context);
    if (!file_uri) {
        goto exit;
    }
    ret = (*env)->CallIntMethod(env, context->obj_stream_protocol, context->jfields.jmd_open, file_uri);
    if (ret != 0) {
        ret = -2;
        goto exit;
    }
    ret = 0;
exit:
    if (object) {
        (*env)->DeleteLocalRef(env, object);
    }

    if (!context->obj_stream_protocol) {
        ff_jni_reset_jfields(env, &context->jfields, jni_stream_protocol_mapping, 1, context);
    }
    return ret;
}

static jobject get_jbuffer_with_check_capacity(URLContext *h, int new_capacity)
{
    JNIEnv *env = NULL;
    STMediaContext *context = h->priv_data;

    if (context->jbuffer && context->jbuffer_capacity >= new_capacity) {
        return context->jbuffer;
    }
    new_capacity = FFMAX(new_capacity, context->jbuffer_capacity * 2);

    env = ff_jni_get_env(context);
    if (!env) {
        return NULL;
    }
    (*env)->DeleteGlobalRef(env, context->jbuffer);
    context->jbuffer_capacity = 0;

    jbyteArray local = (*env)->NewByteArray(env, new_capacity);
    if (!local) {
        return NULL;
    }
    context->jbuffer = (*env)->NewGlobalRef(env, local);
    (*env)->DeleteLocalRef(env, local);

    if (!context->jbuffer) {
        return NULL;
    }
    context->jbuffer_capacity = new_capacity;
    return context->jbuffer;
}

static int stmedia_read(URLContext *h, unsigned char *buf, int size)
{
    STMediaContext *context = h->priv_data;
    int ret = -1;
    JNIEnv *env = NULL;
    jbyteArray jbuffer = NULL;

    env = ff_jni_get_env(context);
    if (!env) {
        goto exit;
    }
    jbuffer = get_jbuffer_with_check_capacity(h, size);
    if (!jbuffer)
        return AVERROR(ENOMEM);

    ret = (*env)->CallIntMethod(env, context->obj_stream_protocol, context->jfields.jmd_read, jbuffer, 0, size);
    if (ret != 0) {
        ret = -2;
        goto exit;
    }
    (*env)->GetByteArrayRegion(env, jbuffer, 0, ret, (jbyte*)buf);
    ret = 0;
exit:

    return ret;
}

static int64_t stmedia_seek(URLContext *h, int64_t pos, int whence)
{
    STMediaContext *context = h->priv_data;
    int ret = -1;
    JNIEnv *env = NULL;

    env = ff_jni_get_env(context);
    if (!env) {
        goto exit;
    }
    ret = (*env)->CallIntMethod(env, context->obj_stream_protocol, context->jfields.jmd_seek, pos);
    if (ret != 0) {
        ret = -2;
        goto exit;
    }
    ret = 0;
exit:
    return ret;
}

static int stmedia_close(URLContext *h)
{
    STMediaContext *context = h->priv_data;
    int ret = -1;
    JNIEnv *env = NULL;

    env = ff_jni_get_env(context);
    if (!env) {
        goto exit;
    }
    (*env)->CallVoidMethod(env, context->obj_stream_protocol, context->jfields.jmd_close);
    ret = 0;
exit:
    return ret;
}

const URLProtocol ff_stmedia_protocol = {
    .name                = "stmedia",
    .url_open2           = stmedia_open,
    .url_read            = stmedia_read,
    .url_seek            = stmedia_seek,
    .url_close           = stmedia_close,
    .priv_data_size      = sizeof(STMediaContext),
    .priv_data_class     = &stmedia_context_class
};

#endif /* CONFIG_STMEDIA_PROTOCOL */