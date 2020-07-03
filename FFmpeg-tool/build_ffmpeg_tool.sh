#!/bin/sh

# Author: AlanWang
# Email: alanwang4523@gmail.com
# Date: 2020-07-01
# @FileName: build_ffmpeg_tool.sh

# MY_NDK 和 MY_CMAKE 需要改成自己对应的 ndk 中的目录
MY_NDK="/Users/wangjianjun/AndroidDev/android-ndk-r15c"
MY_SDK="/Users/wangjianjun/AndroidDev/sdk"
MY_CMAKE="${MY_SDK}/cmake/3.6.3155560/bin/cmake"

if [ -z "$MY_NDK" ]; then
  echo "Please set MY_NDK to the Android NDK folder"
  exit 1
fi

if [ -z "$MY_CMAKE" ]; then
  echo "Please set MY_CMAKE to the Android CMake folder"
  exit 1
fi

OUTPUT_LIBS="./output"


# arme_abis=(armeabi armeabi-v7a arm64-v8a x86 x86_64 mips mips64)


function build_with_armeabi() {
	ARME_ABI=$1
	echo ${ARME_ABI}
	ANDROID_NATIVE_API_LEVEL="android-$2"
	echo ${ANDROID_NATIVE_API_LEVEL}

	BUILD_DIR="./build/android/${ARME_ABI}"
	BUILD_REF_DIR="./build/android/${ARME_ABI}/ref"
	OUTPUT_SO_DIR="${BUILD_DIR}/build/android/libs/${ARME_ABI}"

	echo ${BUILD_DIR}
	rm -r ${BUILD_DIR}

	PRE_EXE_DIR=$(pwd)
	echo ${PRE_EXE_DIR}

	${MY_CMAKE} \
	-H"./" \
	-B"${BUILD_DIR}" \
	-DANDROID_ABI="${ARME_ABI}" \
	-DANDROID_NDK="${MY_NDK}" \
	-DCMAKE_LIBRARY_OUTPUT_DIRECTORY="./build/android/libs/${ARME_ABI}" \
	-DCMAKE_BUILD_TYPE="Release" \
	-DCMAKE_TOOLCHAIN_FILE="${MY_NDK}/build/cmake/android.toolchain.cmake" \
	-DANDROID_NATIVE_API_LEVEL=${ANDROID_NATIVE_API_LEVEL} \
	-DANDROID_TOOLCHAIN="clang" \
	-DCMAKE_C_FLAGS="-fpic -fexceptions -frtti -Wno-narrowing" \
	-DCMAKE_CXX_FLAGS="-fpic -fexceptions -frtti -Wno-narrowing" \
	-DANDROID_STL="c++_static"


	cd ${BUILD_DIR}
	make clean
	make

	cd ${PRE_EXE_DIR}
	mkdir -p ${OUTPUT_LIBS}/${ARME_ABI}/
	cp ${BUILD_DIR}/ffmpeg_tool ${OUTPUT_LIBS}/${ARME_ABI}/
}

build_with_armeabi armeabi-v7a 16
build_with_armeabi arm64-v8a 21
