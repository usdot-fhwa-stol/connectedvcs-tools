import glob
import xml.etree.ElementTree as ET
import pathlib

print("📄 Found jacoco.xml files with the following coverage:\n")
print("Module                          │ Covered / Total │   %")
print("────────────────────────────────────────────────────────────")

reports = glob.glob("**/target/site/jacoco/jacoco.xml", recursive=True)
if not reports:
    print("  ❌ No jacoco.xml files found.")
    exit(1)

for path in sorted(reports):
    try:
        tree = ET.parse(path)
        counter = tree.find(".//counter[@type='LINE']")
        if counter is None:
            print(f"{path:30s} │ No <counter type='LINE'> element")
            continue
        covered = int(counter.get("covered"))
        missed = int(counter.get("missed"))
        total = covered + missed
        pct = 0.0 if total == 0 else round(covered * 100 / total, 1)
        module = pathlib.Path(path).parts[0]
        print(f"{module:30s} │ {covered:6d} / {total:<6d} │ {pct:5.1f}%")
    except Exception as e:
        print(f"⚠️ Error parsing {path}: {e}")