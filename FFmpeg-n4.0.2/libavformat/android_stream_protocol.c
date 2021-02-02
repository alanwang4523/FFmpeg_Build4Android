
#include <jni.h>
#include "libavutil/avassert.h"
#include "libavutil/mem.h"
#include "libavutil/avstring.h"
#include "libavutil/internal.h"
#include "libavutil/opt.h"
#include "avformat.h"
#include "url.h"
#include "libavcodec/ffjni.h"
#include "asp_config.h"

/**
 * Author: AlanWang4523.
 * Date: 2020/11/02 17:50.
 * Mail: alanwang4523@gmail.com
 */

struct JNIStreamProtocolFields {
     jclass class_streamprotocol;
     jmethodID jmd_init;
     jmethodID jmd_open;
     jmethodID jmd_getSize;
     jmethodID jmd_read;
     jmethodID jmd_seek;
     jmethodID jmd_close;
};

struct JNIByteBufferFields {
     jclass class_byte_buffer;
     jmethodID jmd_s_allocate_direct;
};

typedef struct _ASPContext {
    const AVClass *class;
    struct JNIStreamProtocolFields jfields;
    struct JNIByteBufferFields j_buff_fields;
    jobject obj_stream_protocol;
    int64_t media_size;
    jobject obj_direct_buf;
    int jbuffer_capacity;
} ASPContext;

static const AVOption options[] = {
    { NULL }
};

static const AVClass asp_context_class = {
    .class_name = "ASP",
    .item_name  = av_default_item_name,
    .option     = options,
    .version    = LIBAVUTIL_VERSION_INT,
};

#if CONFIG_ASP_PROTOCOL

// #define ASP_CLASS_PATH "com/alan/ffmpegjni4android/protocols/StreamProtocol"

static const struct FFJniField jni_stream_protocol_mapping[] = {
    { ASP_CLASS_PATH, NULL, NULL, FF_JNI_CLASS, offsetof(struct JNIStreamProtocolFields, class_streamprotocol), 1 },
    { ASP_CLASS_PATH, "<init>", "()V", FF_JNI_METHOD, offsetof(struct JNIStreamProtocolFields, jmd_init), 1 },
    { ASP_CLASS_PATH, "open", "(Ljava/lang/String;)I", FF_JNI_METHOD, offsetof(struct JNIStreamProtocolFields, jmd_open), 1 },
    { ASP_CLASS_PATH, "getSize", "()J", FF_JNI_METHOD, offsetof(struct JNIStreamProtocolFields, jmd_getSize), 1 },
    { ASP_CLASS_PATH, "read", "(Ljava/nio/ByteBuffer;II)I", FF_JNI_METHOD, offsetof(struct JNIStreamProtocolFields, jmd_read), 1 },
    { ASP_CLASS_PATH, "seek", "(JI)I", FF_JNI_METHOD, offsetof(struct JNIStreamProtocolFields, jmd_seek), 1 },
    { ASP_CLASS_PATH, "close", "()V", FF_JNI_METHOD, offsetof(struct JNIStreamProtocolFields, jmd_close), 1 },
    { NULL }
};

static const struct FFJniField jni_byte_buffer_mapping[] = {
    { "java/nio/ByteBuffer", NULL, NULL, FF_JNI_CLASS, offsetof(struct JNIByteBufferFields, class_byte_buffer), 1 },
    { "java/nio/ByteBuffer", "allocateDirect", "(I)Ljava/nio/ByteBuffer;", FF_JNI_STATIC_METHOD, offsetof(struct JNIByteBufferFields, jmd_s_allocate_direct), 1 },
    { NULL }
};

static int asp_open(URLContext *h, const char *filename, int flags)
{
    ASPContext *context = h->priv_data;
    int ret = -1;
    JNIEnv *env = NULL;
    jobject object = NULL;
    jstring file_uri = NULL;

    av_strstart(filename, "asp:", &filename);

    env = ff_jni_get_env(context);
    if (!env) {
        goto exit;
    }

    if (ff_jni_init_jfields(env, &context->jfields, jni_stream_protocol_mapping, 1, context) < 0) {
        goto exit;
    }

    if (ff_jni_init_jfields(env, &context->j_buff_fields, jni_byte_buffer_mapping, 1, context) < 0) {
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
        ret = AVERROR(EIO);
        goto exit;
    }

    context->media_size = (*env)->CallLongMethod(env, context->obj_stream_protocol, context->jfields.jmd_getSize);
    if (context->media_size < 0) {
        context->media_size = -1;
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
    ASPContext *context = h->priv_data;
    jobject local_obj;

    if (context->obj_direct_buf && context->jbuffer_capacity >= new_capacity) {
        return context->obj_direct_buf;
    }
    new_capacity = FFMAX(new_capacity, context->jbuffer_capacity * 2);

    env = ff_jni_get_env(context);
    if (!env) {
        return NULL;
    }

    if (context->obj_direct_buf) {
        (*env)->DeleteGlobalRef(env, context->obj_direct_buf);
        context->jbuffer_capacity = 0;
    }

    local_obj = (*env)->CallStaticObjectMethod(env, context->j_buff_fields.class_byte_buffer,
            context->j_buff_fields.jmd_s_allocate_direct, new_capacity);

    if (!local_obj) {
        return NULL;
    }
    context->obj_direct_buf = (*env)->NewGlobalRef(env, local_obj);
    context->jbuffer_capacity = new_capacity;

    (*env)->DeleteLocalRef(env, local_obj);
    return context->obj_direct_buf;
}

static int asp_read(URLContext *h, unsigned char *buf, int size)
{
    ASPContext *context = h->priv_data;
    int ret = -1;
    JNIEnv *env = NULL;
    jobject jbuffer = NULL;
    void * p_buf_data;

    env = ff_jni_get_env(context);
    if (!env) {
        goto exit;
    }
    jbuffer = get_jbuffer_with_check_capacity(h, size);
    if (!jbuffer) {
        ret = AVERROR(ENOMEM);
        goto exit;
    }

    ret = (*env)->CallIntMethod(env, context->obj_stream_protocol, context->jfields.jmd_read, jbuffer, 0, size);
    if (ret < 0) {
        ret = AVERROR(EIO);
        goto exit;
    }

    p_buf_data = (*env)->GetDirectBufferAddress(env, jbuffer);
    memcpy(buf, p_buf_data, ret);

    if (ret == 0) {
        ret = AVERROR_EOF;
    }
exit:
    return ret;
}

static int64_t asp_seek(URLContext *h, int64_t pos, int whence)
{
    ASPContext *context = h->priv_data;
    int ret = -1;
    JNIEnv *env = NULL;

    env = ff_jni_get_env(context);
    if (!env) {
        goto exit;
    }
    if (AVSEEK_SIZE == whence) {
        return context->media_size;
    }
    ret = (*env)->CallIntMethod(env, context->obj_stream_protocol, context->jfields.jmd_seek, pos, whence);
    if (ret != 0) {
        ret = AVERROR(EIO);
        goto exit;
    }
    ret = 0;
exit:
    return ret;
}

static int asp_close(URLContext *h)
{
    ASPContext *context = h->priv_data;
    int ret = -1;
    JNIEnv *env = NULL;

    env = ff_jni_get_env(context);
    if (!env) {
        goto exit;
    }
    (*env)->CallVoidMethod(env, context->obj_stream_protocol, context->jfields.jmd_close);

    (*env)->DeleteGlobalRef(env, context->obj_direct_buf);
    (*env)->DeleteGlobalRef(env, context->obj_stream_protocol);
    ret = 0;
exit:
    return ret;
}

const URLProtocol ff_asp_protocol = {
    .name                = "asp",
    .url_open2           = asp_open,
    .url_read            = asp_read,
    .url_seek            = asp_seek,
    .url_close           = asp_close,
    .priv_data_size      = sizeof(ASPContext),
    .priv_data_class     = &asp_context_class
};

#endif /* CONFIG_ASP_PROTOCOL */