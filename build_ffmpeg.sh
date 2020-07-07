#!/bin/bash

# Author: AlanWang
# Email: alanwang4523@gmail.com
# Date: 2019-06-12


# 需要指定 ANDROID_NDK 和 ANDROID_NDK
export ANDROID_SDK=/Users/wangjianjun/AndroidDev/sdk
export ANDROID_NDK=/Users/wangjianjun/AndroidDev/android-ndk-r15c

HOST_OS_ARCH=darwin-x86_64

function configure_ffmpeg {

  ABI=$1
  API_LEVEL=$2
  TOOLCHAIN_PATH=

  # 配置参考 ： https://gcc.gnu.org/onlinedocs/gcc/ARM-Options.html
  case ${ABI} in
  armeabi-v7a)
    TOOLCHAIN_PREFIX=arm-linux-androideabi
    ARCH=armv7-a

    TOOLCHAIN_PATH=$ANDROID_NDK/toolchains/$TOOLCHAIN_PREFIX-4.9/prebuilt/$HOST_OS_ARCH
    CROSS_PREFIX=$TOOLCHAIN_PATH/bin/$TOOLCHAIN_PREFIX-
    CROSS_GCC_LIB=$TOOLCHAIN_PATH/lib/gcc/$TOOLCHAIN_PREFIX/4.9.x

    SYSROOT=$ANDROID_NDK/platforms/android-$API_LEVEL/arch-arm
    SYSROOT_INC=$SYSROOT/usr/include

    ADDI_CFLAGS="-march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16 -mcpu=cortex-a8"
    ADDI_CXXFLAGS="-march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16 -mcpu=cortex-a8"
    ADDI_LDFLAGS="-Wl,--fix-cortex-a8 -lm -lz"
    EXTRA_CONFIG="--arch=arm --enable-neon --enable-asm --enable-inline-asm"
    ;;
  arm64-v8a)
    TOOLCHAIN_PREFIX=aarch64-linux-android
    ARCH=aarch64

    TOOLCHAIN_PATH=$ANDROID_NDK/toolchains/$TOOLCHAIN_PREFIX-4.9/prebuilt/$HOST_OS_ARCH
    CROSS_PREFIX=$TOOLCHAIN_PATH/bin/$TOOLCHAIN_PREFIX-
    CROSS_GCC_LIB=$TOOLCHAIN_PATH/lib/gcc/$TOOLCHAIN_PREFIX/4.9.x

    SYSROOT=$ANDROID_NDK/platforms/android-$API_LEVEL/arch-arm64
    SYSROOT_INC=$SYSROOT/usr/include

    ADDI_CFLAGS="-march=armv8-a"
    ADDI_CXXFLAGS="-march=armv8-a"
    ADDI_LDFLAGS="-lm -lz"
    EXTRA_CONFIG="--enable-neon --enable-asm --enable-inline-asm"
    ;;
  x86)
    TOOLCHAIN_PREFIX=i686-linux-android
    ARCH=x86

    TOOLCHAIN_PATH=$ANDROID_NDK/toolchains/x86-4.9/prebuilt/$HOST_OS_ARCH
    CROSS_PREFIX=$TOOLCHAIN_PATH/bin/$TOOLCHAIN_PREFIX-
    CROSS_GCC_LIB=$TOOLCHAIN_PATH/lib/gcc/$TOOLCHAIN_PREFIX/4.9.x

    SYSROOT=$ANDROID_NDK/platforms/android-$API_LEVEL/arch-x86
    SYSROOT_INC=$SYSROOT/usr/include

    ADDI_CFLAGS="-march=atom -msse3 -mfpmath=sse"
    ADDI_CXXFLAGS="-march=atom -msse3 -mfpmath=sse"
    ADDI_LDFLAGS="-lm -lz"
    EXTRA_CONFIG="--disable-asm --disable-amd3dnow --disable-avx"
    ;;
  x86_64)
    TOOLCHAIN_PREFIX=x86_64-linux-android
    ARCH=x86_64

    TOOLCHAIN_PATH=$ANDROID_NDK/toolchains/x86_64-4.9/prebuilt/$HOST_OS_ARCH
    CROSS_PREFIX=$TOOLCHAIN_PATH/bin/$TOOLCHAIN_PREFIX-
    CROSS_GCC_LIB=$TOOLCHAIN_PATH/lib/gcc/$TOOLCHAIN_PREFIX/4.9.x

    SYSROOT=$ANDROID_NDK/platforms/android-$API_LEVEL/arch-x86_64
    SYSROOT_INC=$SYSROOT/usr/include

    ADDI_CFLAGS="-march=atom -msse3 -mfpmath=sse"
    ADDI_CXXFLAGS="-march=atom -msse3 -mfpmath=sse"
    ADDI_LDFLAGS="-lm -lz"
    EXTRA_CONFIG="--disable-asm --disable-amd3dnow --disable-avx"
    ;;
  esac


  # TARGET_PREFIX=$(pwd)/android_build/${ABI}
  TARGET_PREFIX=$(pwd)/../libs/ffmpeg-out/${ABI}
  X264_LIB_PATH=$(pwd)/../libs/openh264-out/${ABI}

  if [[ -f $X264_LIB_PATH/lib/libopenh264.so ]]; then
    rm $X264_LIB_PATH/lib/libopenh264.so
    echo "rm libopenh264.so "
  fi

  echo "Output prefix ${TARGET_PREFIX}"
  echo "Configuring FFmpeg build for ${ABI}"
  echo "SYSROOT : $SYSROOT"
  echo "CROSS_PREFIX : $CROSS_PREFIX"
  echo "X264_LIB_PATH : ${X264_LIB_PATH}"

  ./configure \
    --prefix=${TARGET_PREFIX} \
    --enable-gpl \
    --disable-shared \
    --enable-static \
    --disable-small \
    --disable-stripping \
    --disable-debug \
    --enable-ffmpeg \
    --enable-pthreads \
    --disable-w32threads \
    --disable-os2threads \
    --disable-ffplay \
    --disable-ffprobe \
    --disable-avdevice \
    --disable-devices \
    --disable-symver \
    --disable-doc \
    --disable-decoders \
    --disable-encoders \
    --disable-parsers \
    --disable-protocols \
    --enable-protocol=file \
    --enable-parser=aac \
    --enable-parser=mpegaudio \
    --enable-parser=h264\
    --disable-devices \
    --enable-decoder=aac \
    --enable-decoder=pcm_s16le \
    --enable-decoder=pcm_f32le \
    --enable-decoder=h264\
    --enable-decoder=png \
    --enable-decoder=mjpeg \
    --enable-encoder=aac \
    --enable-encoder=pcm_s16le \
    --enable-encoder=pcm_f32le \
    --enable-libopenh264 \
    --enable-encoder=libopenh264 \
    --enable-decoder=libopenh264 \
    --disable-bsfs \
    --enable-bsf=aac_adtstoasc \
    --enable-bsf=hevc_mp4toannexb \
    --enable-bsf=mpeg4_unpack_bframes \
    --enable-bsf=h264_mp4toannexb \
    --disable-muxers \
    --enable-muxer=mp4 \
    --enable-muxer=wav \
    --enable-muxer=adts \
    --disable-demuxers \
    --enable-demuxer=h264 \
    --enable-demuxer=aac \
    --enable-demuxer=wav \
    --enable-demuxer=flv \
    --enable-demuxer=avi \
    --enable-demuxer=mov \
    --enable-demuxer=matroska \
    --enable-demuxer=concat \
    --enable-demuxer=image2 \
    --disable-filters \
    --enable-filter=aresample \
    --enable-filter=transpose \
    --enable-filter=crop \
    --enable-filter=scale \
    --enable-filter=hflip \
    --enable-filter=vflip \
    --enable-filter=overlay \
    --enable-zlib \
    --enable-runtime-cpudetect \
    --cross-prefix=$CROSS_PREFIX \
    --target-os=android \
    --arch=$ARCH \
    --enable-cross-compile \
    --sysroot=$SYSROOT \
    --extra-cflags="-O3 -ffast-math -fPIC $ADDI_CFLAGS -I$X264_LIB_PATH/include -I$SYSROOT_INC" \
    --extra-cxxflags="-O3 -ffast-math -fPIC $ADDI_CXXFLAGS -I$X264_LIB_PATH/include -I$SYSROOT_INC" \
    --extra-ldflags="$ADDI_LDFLAGS -L$X264_LIB_PATH/lib" \
    --extra-ldexeflags="-pie -fPIC $ADDI_LDFLAGS -L$X264_LIB_PATH/lib" \
    ${EXTRA_CONFIG}

  return $?
}

function merge_static_to_share
{
  ${CROSS_PREFIX}ld \
    -rpath-link=$SYSROOT/usr/lib \
    -L$SYSROOT/usr/lib \
    -soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive -o \
    $TARGET_PREFIX/lib/libffmpeg.so \
    $TARGET_PREFIX/lib/libavcodec.a \
    $TARGET_PREFIX/lib/libavfilter.a \
    $TARGET_PREFIX/lib/libswresample.a \
    $TARGET_PREFIX/lib/libavformat.a \
    $TARGET_PREFIX/lib/libavutil.a \
    $TARGET_PREFIX/lib/libswscale.a \
    $TARGET_PREFIX/lib/libpostproc.a \
    ${X264_LIB_PATH}/lib/libopenh264.a \
    -lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker \
    $CROSS_GCC_LIB/libgcc.a
}


function build_ffmpeg_static {
  configure_ffmpeg $1 $2
  if [ $? -eq 0 ]
  then
      make clean
      make -j12
      make install

      merge_static_to_share
  else
      echo "FFmpeg build failed, please check the configurations and error log."
  fi
}


function build_ffmpeg {
  build_ffmpeg_static  $1 $2
  ${CROSS_PREFIX}strip $TARGET_PREFIX/lib/libffmpeg.so
}


ROOT_PATH=$(pwd)
FFMPGE_SOURCE_DIR=$ROOT_PATH/FFmpeg-n4.0.2

if [[ ! -d $FFMPGE_SOURCE_DIR ]]; then
  echo "Did not found $FFMPGE_SOURCE_DIR"
  exit 1
fi

cd $FFMPGE_SOURCE_DIR

build_ffmpeg armeabi-v7a 16
build_ffmpeg arm64-v8a 21
