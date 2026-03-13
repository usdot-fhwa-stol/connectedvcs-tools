cmake_minimum_required(VERSION 3.14)
project(connectedvcs-jni VERSION 1.0.0 LANGUAGES C)


set(SAEJ2735_SPEC_VERSION "2024" CACHE STRING
    "SAE J2735 specification version (e.g., 2024, 2020, 201603)")

set(STOL_J2735_PACKAGE_NAME "stol-j2735-${SAEJ2735_SPEC_VERSION}"
    CACHE STRING "CMake package name for the stol-j2735 library")

set(STOL_J2735_LINK_TARGET "::${STOL_J2735_PACKAGE_NAME}"
    CACHE STRING "CMake imported target to link against")

# Output library name — Java side will call System.loadLibrary("asn1c_jni")
set(OUTPUT_LIB_NAME "asn1c_jni" CACHE STRING
    "Name of the output shared library (without lib prefix / .so suffix)")


list(APPEND CMAKE_PREFIX_PATH "/opt/carma/cmake")
list(APPEND CMAKE_MODULE_PATH "/opt/carma/lib")

find_package(${STOL_J2735_PACKAGE_NAME} REQUIRED)

if(DEFINED ENV{JAVA_HOME})
    set(JAVA_HOME $ENV{JAVA_HOME})
endif()
set(JNI_INCLUDE_DIRS "${JAVA_HOME}/include" "${JAVA_HOME}/include/linux")

set(JNI_WRAPPER_DIR "${CMAKE_CURRENT_SOURCE_DIR}/src/main/java")

set(JNI_WRAPPER_SOURCES
    ${JNI_WRAPPER_DIR}/wrapper.c
    ${JNI_WRAPPER_DIR}/decoder_wrapper.c
    ${JNI_WRAPPER_DIR}/tim_wrapper.c
    ${JNI_WRAPPER_DIR}/tim_decoder_wrapper.c
    ${JNI_WRAPPER_DIR}/rga_wrapper.c
)

set(JNI_WRAPPER_HEADERS
    ${JNI_WRAPPER_DIR}/gov_usdot_cv_mapencoder_Encoder.h
    ${JNI_WRAPPER_DIR}/gov_usdot_cv_asn1decoder_Decoder.h
    ${JNI_WRAPPER_DIR}/gov_usdot_cv_asn1decoder_TIMDecoder.h
    ${JNI_WRAPPER_DIR}/gov_usdot_cv_timencoder_Encoder.h
    ${JNI_WRAPPER_DIR}/gov_usdot_cv_rgaencoder_Encoder.h
)

add_library(${OUTPUT_LIB_NAME} SHARED
    ${JNI_WRAPPER_SOURCES}
    ${JNI_WRAPPER_HEADERS}
)

target_include_directories(${OUTPUT_LIB_NAME} PRIVATE
    # JNI wrapper headers (gov_usdot_cv_*.h files)
    ${JNI_WRAPPER_DIR}
    # JNI headers (jni.h, jni_md.h)
    ${JNI_INCLUDE_DIRS}
    # stol-j2735 headers (MessageFrame.h, TravelerInformation.h, etc.)
    /opt/carma/include/${STOL_J2735_PACKAGE_NAME}
)

target_link_libraries(${OUTPUT_LIB_NAME} PRIVATE
    ${STOL_J2735_LINK_TARGET}
)

# Position-independent code (required for shared libraries, matches -fPIC)
set_target_properties(${OUTPUT_LIB_NAME} PROPERTIES
    POSITION_INDEPENDENT_CODE ON
    # Output directly to lib/ for easy integration
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/lib"
)

install(TARGETS ${OUTPUT_LIB_NAME}
    LIBRARY DESTINATION lib
)

message(STATUS "")
message(STATUS "=== connectedvcs-jni build configuration ===")
message(STATUS "  J2735 spec version : ${SAEJ2735_SPEC_VERSION}")
message(STATUS "  stol-j2735 package : ${STOL_J2735_PACKAGE_NAME}")
message(STATUS "  Link target        : ${STOL_J2735_LINK_TARGET}")
message(STATUS "  Output library     : lib${OUTPUT_LIB_NAME}.so")
message(STATUS "  Output directory   : ${CMAKE_CURRENT_SOURCE_DIR}/third_party_lib")
message(STATUS "  JNI include        : ${JNI_INCLUDE_DIRS}")
message(STATUS "  Wrapper sources    : ${JNI_WRAPPER_SOURCES}")
message(STATUS "")