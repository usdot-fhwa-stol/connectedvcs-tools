#!/usr/bin/env python3
"""
GDAL Georeferencing Script
Georeferences an image using Ground Control Points (GCPs)
"""

import sys
import json
import base64
import tempfile
import os
from typing import List, Dict, Any
from pathlib import Path

try:
    from osgeo import gdal, osr
    gdal.UseExceptions()
except ImportError:
    print("Error: GDAL Python bindings not found. Install with: pip install GDAL")
    sys.exit(1)


def georeference_image(input_image_path: str, gcps: List[Dict], output_dir: str) -> Dict[str, Any]:
    """
    Georeference an image using Ground Control Points
    
    Args:
        input_image_path: Path to the input image
        gcps: List of Ground Control Points with imageX, imageY, longitude, latitude
        output_dir: Directory to save the georeferenced image
        
    Returns:
        Dictionary with processing results
    """
    try:
        # Open the input image
        src_ds = gdal.Open(input_image_path, gdal.GA_ReadOnly)
        if src_ds is None:
            raise Exception(f"Could not open image file: {input_image_path}")
        
        # Get image dimensions
        width = src_ds.RasterXSize
        height = src_ds.RasterYSize
        bands = src_ds.RasterCount
        
        # Create GCP list for GDAL
        gcp_list = []
        for i, gcp in enumerate(gcps):
            # GDAL GCP: (pixel_x, pixel_y, elevation, geo_x, geo_y, info, id)
            gdal_gcp = gdal.GCP(
                float(gcp['longitude']),  # geo_x (longitude)
                float(gcp['latitude']),   # geo_y (latitude)  
                0.0,                      # elevation (Z)
                float(gcp['imageX']),     # pixel_x
                float(gcp['imageY']),     # pixel_y
                f"GCP_{i+1}",            # info
                gcp['pointId']           # id
            )
            gcp_list.append(gdal_gcp)
        
        # Create spatial reference (WGS84)
        srs = osr.SpatialReference()
        srs.ImportFromEPSG(4326)  # WGS84
        
        # Create output file path
        output_filename = f"georeferenced_{Path(input_image_path).stem}.tif"
        output_path = os.path.join(output_dir, output_filename)
        
        # Create output dataset
        driver = gdal.GetDriverByName('GTiff')
        dst_ds = driver.Create(
            output_path,
            width, height, bands,
            src_ds.GetRasterBand(1).DataType
        )
        
        # Copy image data
        for i in range(1, bands + 1):
            src_band = src_ds.GetRasterBand(i)
            dst_band = dst_ds.GetRasterBand(i)
            data = src_band.ReadAsArray()
            dst_band.WriteArray(data)
        
        # Set GCPs and projection
        dst_ds.SetGCPs(gcp_list, srs.ExportToWkt())
        
        # Perform georeferencing transformation using gdalwarp
        temp_warped = os.path.join(output_dir, f"warped_{output_filename}")
        
        # Use gdal.Warp for automatic georeferencing
        warp_options = gdal.WarpOptions(
            format='GTiff',
            dstSRS='EPSG:4326',
            resampleAlg=gdal.GRA_Bilinear,
            tps=True  # Thin Plate Spline transformation
        )
        
        gdal.Warp(temp_warped, dst_ds, options=warp_options)
        
        # Clean up intermediate files
        dst_ds = None
        src_ds = None
        os.remove(output_path)
        
        # Read the georeferenced image and convert to base64
        with open(temp_warped, 'rb') as f:
            image_data = f.read()
            base64_image = base64.b64encode(image_data).decode('utf-8')
        
        # Get extent information
        warped_ds = gdal.Open(temp_warped, gdal.GA_ReadOnly)
        geotransform = warped_ds.GetGeoTransform()
        
        # Calculate extent
        min_x = geotransform[0]
        max_y = geotransform[3]
        max_x = min_x + (warped_ds.RasterXSize * geotransform[1])
        min_y = max_y + (warped_ds.RasterYSize * geotransform[5])
        
        extent = {
            'minLongitude': min_x,
            'maxLongitude': max_x,
            'minLatitude': min_y,
            'maxLatitude': max_y
        }
        
        warped_ds = None
        
        # Clean up temporary file
        os.remove(temp_warped)
        
        return {
            'success': True,
            'processedImageBase64': base64_image,
            'extent': extent,
            'gcpCount': len(gcps),
            'imageWidth': width,
            'imageHeight': height,
            'projection': 'EPSG:4326'
        }
        
    except Exception as e:
        return {
            'success': False,
            'error': str(e),
            'gcpCount': len(gcps) if gcps else 0
        }


def main():
    """Main function to handle command line execution"""
    if len(sys.argv) != 4:
        print("Usage: python3 georeference.py <image_path> <gcps_json> <output_dir>")
        sys.exit(1)
    
    image_path = sys.argv[1]
    gcps_json = sys.argv[2]
    output_dir = sys.argv[3]
    
    try:
        # Parse GCPs from JSON
        gcps = json.loads(gcps_json)
        
        # Validate GCP structure
        required_fields = ['pointId', 'imageX', 'imageY', 'longitude', 'latitude']
        for gcp in gcps:
            for field in required_fields:
                if field not in gcp:
                    raise ValueError(f"Missing required field '{field}' in GCP: {gcp}")
        
        # Ensure output directory exists
        os.makedirs(output_dir, exist_ok=True)
        
        # Process the image
        result = georeference_image(image_path, gcps, output_dir)
        
        # Output result as JSON
        print(json.dumps(result))
        
    except json.JSONDecodeError as e:
        error_result = {
            'success': False,
            'error': f"Invalid JSON in GCPs parameter: {e}"
        }
        print(json.dumps(error_result))
        sys.exit(1)
        
    except Exception as e:
        error_result = {
            'success': False,
            'error': str(e)
        }
        print(json.dumps(error_result))
        sys.exit(1)


if __name__ == '__main__':
    main()