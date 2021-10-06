#!/bin/bash
npm install $(npm pack ../ | tail -1)
