

#include "libavutil/avstring.h"
#include "libavutil/internal.h"
#include "libavutil/opt.h"
#include "avformat.h"
#if HAVE_DIRENT_H
#include <dirent.h>
#endif
#include <fcntl.h>
#if HAVE_IO_H
#include <io.h>
#endif
#if HAVE_UNISTD_H
#include <unistd.h>
#endif
#include <sys/stat.h>
#include <stdlib.h>
#include "os_support.h"
#include "url.h"


/**
 * Author: AlanWang4523.
 * Date: 2020/11/02 17:50.
 * Mail: alanwang4523@gmail.com
 */

typedef struct _STMediaContext {
    const AVClass *class;
    int fd;
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

static int stmedia_open(URLContext *h, const char *filename, int flags)
{
    STMediaContext *c = h->priv_data;
    int fd;

    av_strstart(filename, "stmedia:", &filename);

    fd = avpriv_open(filename, O_RDONLY, 0666);
    if (fd == -1)
        return AVERROR(errno);
    c->fd = fd;

    return 0;
}

static int stmedia_read(URLContext *h, unsigned char *buf, int size)
{
    STMediaContext *c = h->priv_data;
    int ret;
    ret = read(c->fd, buf, size);
    if (ret == 0)
        return AVERROR_EOF;
    return (ret == -1) ? AVERROR(errno) : ret;
}

static int64_t stmedia_seek(URLContext *h, int64_t pos, int whence)
{
    STMediaContext *c = h->priv_data;
    int64_t ret;

    if (whence == AVSEEK_SIZE) {
        struct stat st;
        ret = fstat(c->fd, &st);
        return ret < 0 ? AVERROR(errno) : (S_ISFIFO(st.st_mode) ? 0 : st.st_size);
    }

    ret = lseek(c->fd, pos, whence);

    return ret < 0 ? AVERROR(errno) : ret;
}

static int stmedia_close(URLContext *h)
{
    STMediaContext *c = h->priv_data;
    return close(c->fd);
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