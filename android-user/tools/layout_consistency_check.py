#!/usr/bin/env python3
"""
Layout-Consistency Checker
Scan layout XMLs and corresponding Java/Kotlin sources to validate that
container view IDs (layoutX) map to the same view type in code.

This is a heuristic checker and not a full compiler-level verification.
"""
import os
import re
import sys
import xml.etree.ElementTree as ET

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
# The project structure has the android app under android-user/
LAYOUT_DIR = os.path.join(ROOT, "android-user", "app", "src", "main", "res", "layout")
JAVA_DIR = os.path.join(ROOT, "android-user", "app", "src", "main", "java")

# Map: id_name -> set of xml_tags observed across layouts
layout_id_to_tags = {}

def discover_layout_ids():
    global layout_id_to_tags
    layout_id_to_tags = {}
    if not os.path.isdir(LAYOUT_DIR):
        print(f"[warn] Layout directory not found: {LAYOUT_DIR}")
        return
    for root, _, files in os.walk(LAYOUT_DIR):
        for f in files:
            if not f.endswith('.xml'):
                continue
            path = os.path.join(root, f)
            try:
                tree = ET.parse(path)
                root_el = tree.getroot()
            except Exception as e:
                print(f"[error] Failed parse XML {path}: {e}")
                continue
            for el in root_el.iter():
                # Android id attribute with namespace
                id_attr = el.attrib.get('{http://schemas.android.com/apk/res/android}id')
                if id_attr is None:
                    id_attr = el.attrib.get('android:id')
                if not id_attr:
                    continue
                # id string could be "@+id/layoutX" or "@id/layoutX"
                m = re.match(r'@\+?id/(\w+)', id_attr)
                if not m:
                    continue
                id_name = m.group(1)
                # tag name (e.g., FrameLayout, LinearLayout, etc.)
                tag_name = el.tag.split('}')[-1]  # strip namespace if any
                layout_id_to_tags.setdefault(id_name, set()).add(tag_name)
    print("[layout-check] Discovered IDs:")
    for k in sorted(layout_id_to_tags.keys()):
        tags = layout_id_to_tags[k]
        print(f"  {k}: {sorted(list(tags))}")

def extract_code_var_types():
    # Map: var_name -> declared_type
    var_types = {}
    if not os.path.isdir(JAVA_DIR):
        print(f"[warn] Java source directory not found: {JAVA_DIR}")
        return var_types
    pattern = re.compile(r"private\s+([A-Za-z0-9_<>,]+)\s+([A-Za-z_][A-Za-z0-9_]*)\s*;")
    for root, _, files in os.walk(JAVA_DIR):
        for f in files:
            if not f.endswith('.java'):
                continue
            path = os.path.join(root, f)
            try:
                content = open(path, 'r', encoding='utf-8').read()
            except Exception:
                continue
            for m in pattern.finditer(content):
                decl_type = m.group(1).strip()
                var_name = m.group(2).strip()
                # Keep only the most common declaration type per variable name by last-wins
                var_types[var_name] = decl_type
    return var_types

def analyze():
    discover_layout_ids()
    var_types = extract_code_var_types()

    mismatches = []
    ambiguous_found = False
    # Iterate over all discovered IDs and compare with code declarations
    for id_name, tag_set in layout_id_to_tags.items():
        if len(tag_set) != 1:
            # Ambiguous layout tag; mark issue and report
            print(f"[layout-check] Ambiguous layout id '{id_name}' across layouts: {sorted(list(tag_set))}")
            ambiguous_found = True
            continue
        xml_tag = next(iter(tag_set))
        if id_name in var_types:
            code_type = var_types[id_name]
            # Normalize, keep only simple class name (remove package prefixes)
            simple_code_type = code_type.split('<')[0]  # drop generics if any
            simple_code_type = simple_code_type.split('.')[-1]
            if simple_code_type != xml_tag:
                mismatches.append((id_name, xml_tag, code_type))
        else:
            # No Java/Kotlin field found for this id
            mismatches.append((id_name, xml_tag, None))

    if mismatches or ambiguous_found:
        print("\n[layout-check] Issues found:")
        for item in sorted(mismatches, key=lambda x: x[0]):
            id_name, xml_tag, code_type = item
            if code_type is None:
                print(f"- id '{id_name}' in layout is not referenced in code (expected {xml_tag})")
            else:
                print(f"- id '{id_name}': XML tag={xml_tag}, code_type={code_type}")
        if ambiguous_found:
            print("[layout-check] Note: Some IDs are ambiguous across layouts; consider unifying layout usage.")
        exit_code = 1
    else:
        print("[layout-check] OK: no layout inconsistencies.")
        exit_code = 0
    sys.exit(exit_code)
