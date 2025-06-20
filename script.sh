sudo rm -rf /home/natarajas2/connectedvcs-tools/fedgov-cv-rgaencoder/logs/cv-mapencoder.log
sudo rm -rf /home/natarajas2/connectedvcs-tools/fedgov-cv-mapencoder/applogs
sudo rm -rf /home/natarajas2/connectedvcs-tools/fedgov-cv-message-builder/logs/cv-message-builder.log
 
################################################################
# Copy the RGA JNI files from maptool to asn1c library
cp /home/natarajas2/connectedvcs-tools/fedgov-cv-lib-asn1c/src/main/java/rga_wrapper.c /home/natarajas2/standards/j2735-standard-releases/2024-asn1/src/
cp /home/natarajas2/connectedvcs-tools/fedgov-cv-lib-asn1c/src/main/java/gov_usdot_cv_rgaencoder_Encoder.h /home/natarajas2/standards/j2735-standard-releases/2024-asn1/include/
 
# # Compile and generate .so file
cd /home/natarajas2/standards/j2735-standard-releases/2024-asn1
gcc -g -DPDU=MessageFrame  -DASN_EMIT_DEBUG=0 -shared -o libasn1c_rga.so -I./include/ -I/usr/lib/jvm/java-8-openjdk-amd64/include -I/usr/lib/jvm/java-8-openjdk-amd64/include/linux/ ./src/*.c -fPIC
 
# # Move the generate .so file back to maptool
mv /home/natarajas2/standards/j2735-standard-releases/2024-asn1/libasn1c_rga.so /home/natarajas2/connectedvcs-tools/fedgov-cv-lib-asn1c/third_party_lib/
 
# Set up path
LD_LIBRARY_PATH="/home/natarajas2/connectedvcs-tools/fedgov-cv-lib-asn1c/third_party_lib"
export LD_LIBRARY_PATH
 
# Maven run asn1c 
cd /home/natarajas2/connectedvcs-tools/fedgov-cv-lib-asn1c/
mvn clean install
 
# Maven run unit test
cd /home/natarajas2/connectedvcs-tools/fedgov-cv-mapencoder/
mvn clean install
 
cd /home/natarajas2/connectedvcs-tools/fedgov-cv-rgaencoder/
mvn clean install
 
#cd /home/natarajas2/connectedvcs-tools/fedgov-cv-message-builder/
#mvn clean install
