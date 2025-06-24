const fs = require('fs').promises;
const path = require('path');

const MD_RELATIVE_PATH = 'docs';
const IMG_RELATIVE_PATH = 'docs/img';
const IMAGE_EXTENSIONS = new Set(['png', 'jpg', 'jpeg', 'gif', 'svg']);
const MD_IMG_PATTERN = /!\[.*?]\((.*?)\)|<img\s+[^>]*src=["'](.*?)["'][^>]*>|<Image\s+[^>]*src=["'](.*?)["'][^>]*>/g;

function log(level, message) {
    const timestamp = new Date().toISOString();
    console.log(`${timestamp} [${level}] ${message}`);
}

function normalizeImageKey(baseDir, fullPath) {
    const relative = path.relative(baseDir, fullPath).split(path.sep);
    return relative.length > 1
        ? `${relative[relative.length - 2]}/${relative[relative.length - 1]}`.replace(/\\/g, '/')
        : relative[0].replace(/\\/g, '/');
}

async function walkDirectory(startDir, onFile) {
    try {
        const entries = await fs.readdir(startDir, { withFileTypes: true });
        for (const entry of entries) {
            const fullPath = path.join(startDir, entry.name);
            if (entry.isDirectory()) {
                await walkDirectory(fullPath, onFile);
            } else if (entry.isFile()) {
                await onFile(fullPath);
            }
        }
    } catch (err) {
        log('WARNING', `Failed to read directory "${startDir}": ${err.message}`);
    }
}

async function scanImages(imagesDir) {
    const images = new Set();
    await walkDirectory(imagesDir, async (filePath) => {
        const ext = path.extname(filePath).slice(1).toLowerCase();
        if (IMAGE_EXTENSIONS.has(ext)) {
            const key = normalizeImageKey(imagesDir, filePath);
            images.add(key);
        }
    });
    return images;
}

async function parseResources(rootDir) {
    const resources = new Set();
    await walkDirectory(rootDir, async (filePath) => {
        const fileName = filePath.toLowerCase();
        if (fileName.endsWith('.md') || fileName.endsWith('.mdx')) {
            try {
                const content = await fs.readFile(filePath, 'utf8');
                let match;
                while ((match = MD_IMG_PATTERN.exec(content)) !== null) {
                    const rawPath = match[1] || match[2] || match[3];
                    if (rawPath) {
                        const normalized = path.normalize(rawPath);
                        const folder = path.basename(path.dirname(normalized));
                        const file = path.basename(normalized);
                        const key = folder === '.' ? file : `${folder}/${file}`;
                        resources.add(key);
                    }
                }
            } catch (err) {
                log('WARNING', `Cannot read file "${filePath}": ${err.message}`);
            }
        }
    });
    return resources;
}

async function main() {
    const docsPath = path.resolve(process.cwd(), MD_RELATIVE_PATH);
    const imagesPath = path.resolve(process.cwd(), IMG_RELATIVE_PATH);

    for (const dir of [docsPath, imagesPath]) {
        try {
            await fs.stat(dir);
        } catch {
            log('ERROR', `Directory does not exist: "${dir}"`);
            process.exit(1);
        }
    }

    log('INFO', `Scanning Markdown files in "${docsPath}"...`);
    const usedResources = await parseResources(docsPath);

    log('INFO', `Scanning image files in "${imagesPath}"...`);
    const allImages = await scanImages(imagesPath);

    const unusedImages = [...allImages].filter((img) => !usedResources.has(img));

    if (unusedImages.length === 0) {
        log('SUCCESS', 'No unused images found.');
    } else {
        unusedImages.forEach((img) => console.log(img));
    }
}

main();
