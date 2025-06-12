sudo rm -rf fedgov-cv-mapencoder/logs/cv-mapencoder.log
sudo rm -rf fedgov-cv-mapencoder/applogs
sudo rm -rf fedgov-cv-message-builder/logs/cv-message-builder.log
 
################################################################
# Copy the RGA JNI files from maptool to asn1c library
# cp /home/carma/maptool_ws/public/connectedvcs-tools/fedgov-cv-lib-asn1c/src/main/java/rga_wrapper.c /home/carma/maptool_ws/j2945a_asn1c_lib/standards/j2735-standard-releases/2024-asn1/src/
# cp /home/carma/maptool_ws/public/connectedvcs-tools/fedgov-cv-lib-asn1c/src/main/java/gov_usdot_cv_rgaencoder_Encoder.h /home/carma/maptool_ws/j2945a_asn1c_lib/standards/j2735-standard-releases/2024-asn1/include/
 
# # Compile and generate .so file
# cd /home/carma/maptool_ws/j2945a_asn1c_lib/standards/j2735-standard-releases/2024-asn1
# gcc -g -DPDU=MessageFrame  -DASN_EMIT_DEBUG=0 -shared -o libasn1c_rga.so -I./include/ -I/usr/lib/jvm/java-8-openjdk-amd64/include -I/usr/lib/jvm/java-8-openjdk-amd64/include/linux/ ./src/*.c -fPIC
 
# # Move the generate .so file back to maptool
# mv /home/carma/maptool_ws/j2945a_asn1c_lib/standards/j2735-standard-releases/2024-asn1/libasn1c_rga.so /home/carma/maptool_ws/public/connectedvcs-tools/fedgov-cv-lib-asn1c/third_party_lib/
 
# Set up path
LD_LIBRARY_PATH="fedgov-cv-lib-asn1c/third_party_lib"
export LD_LIBRARY_PATH
 
# Maven run asn1c 
cd fedgov-cv-lib-asn1c/
mvn clean install
 
# Maven run unit test
cd fedgov-cv-rgaencoder/
mvn clean install
 