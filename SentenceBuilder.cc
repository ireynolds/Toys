// Analyzes a text and constructs a graph of n-grams and their contexts in the text. 
// Then generates a sentence by following a path through the graph.


/* ------------------------------------------------- */


// Makefile:

// all:
//   g++ -Wall -std=gnu++0x -g -o ex12 *.cc

// clean:
//   rm ex12 ex12_isaacr.tar.gz

// run:
//   ./ex12 3 ./datafiles/subset/

// test:
//   ./ex12 2 ./smalldatafiles

// tar:
//   tar czvf ex12_isaacr.tar.gz *.h *.cc



// Test data:
// 
// My many dogs have many fleas I think.
// My many fleas have many dogs I think.



/* ------------------------------------------------- */





#include <memory>
#include <fstream>
#include <assert.h>
#include <map>
#include <sstream>
#include <vector>
#include <iostream>
#include <algorithm>
#include <cstdlib>

using namespace std;

#include "SentenceBuilder.h"

string vecToString(vector<string> tokens);

//======================================================================
// SentenceBuilder
//

// unique_ptr<Gram> _root;
// uint32_t _n;

SentenceBuilder::SentenceBuilder(string& filename, uint32_t n) : _root(new Gram()), _n(n) {

    // Open file
    fstream inputFile;
    inputFile.open(filename, ios::in);
    assert(inputFile.is_open());

    // Stores existing grams
    map<string, shared_ptr<Gram> > grams;
    vector<string> tokens;

    // Start the sentence
    shared_ptr<Gram> prev = startSentence(grams, inputFile);
    if (prev == NULL) {
        inputFile.close();
        return;
    }
    tokens = prev->_tokens;
    _root->_children.push_back(prev);

    // Create the rest of the n-grams
    string text;
    while (true) {
        // Modify tokens to hold the next gram
        inputFile >> text;
        if (text == "" || !inputFile.good()) break;
        tokens.erase(tokens.begin());
        tokens.push_back(text);

        // Add the current node if necessary, then add this edge
        shared_ptr<Gram> nextGram = GetDefaultOrAdd(grams, tokens);
        //cout << prev->to_str() << "--> " << nextGram->to_str() << endl;
        prev->_children.push_back(nextGram);
        prev = nextGram;

        if (text.find('.') != string::npos) {
            prev = startSentence(grams, inputFile);
            if (prev == NULL) {
                break;
            }            
            tokens = prev->_tokens;
        }
    }

    inputFile.close();
}

shared_ptr<Gram> SentenceBuilder::startSentence(
                                map<string, shared_ptr<Gram> >& grams,
                                fstream& file) {

    vector<string> tokens;
    string text;

    shared_ptr<Gram> prev = _root;

    // Extract the leading n-gram
    uint32_t i = 0;
    while (i < _n) {
        file >> text;

        // End of file OR end of sentence (either way, before reaching n!)
        if (text == "" || !file.good() || text.find('.') != string::npos) {
            break;
        }
        
        tokens.push_back(text);
        ++i;

        // For each token we add to the beginning of this sentence,
        // add a node to the graph.
        shared_ptr<Gram> next = GetDefaultOrAdd(grams, tokens);
        prev->_children.push_back(next);

        //cout << prev->to_str() << "--> " << next->to_str() <<  endl;
        prev = next;
    }
    
    // Under what circumstances did we stop adding to this n-gram?
    if (i == _n) {
        return prev;
    } else if (!file.good()) {
        return shared_ptr<Gram>(NULL);
    } else { // text.find('.') != string::npos
        return startSentence(grams, file);
    }
}

shared_ptr<Gram> SentenceBuilder::GetDefaultOrAdd(
                                map<string, shared_ptr<Gram> >& grams,
                                vector<string> tokens) {
    shared_ptr<Gram> gram;
    string s = ::vecToString(tokens);
    if (grams.find(s) != grams.end()) {
        gram = grams[s];
    } else {
        gram = shared_ptr<Gram>(new Gram());
        gram->_tokens = tokens;
        pair<string, shared_ptr<Gram> > pair(gram->to_str(), gram);
        grams.insert(pair);
    }
    return gram;
}

SentenceBuilder::~SentenceBuilder() {
    vector<shared_ptr<Gram> > grams;
    recursiveDelete(grams, _root);
}

void SentenceBuilder::recursiveDelete(vector<shared_ptr<Gram> >& grams,
                                      shared_ptr<Gram> curr) {
    //cout << "Adding " << ::vecToString(curr->_tokens) << endl;
    grams.push_back(curr);

    vector<shared_ptr<Gram> > ptrs = curr->_children;
    curr->_children.clear();
    uint32_t i;
    for (i = 0; i < ptrs.size(); ++i) {
        recursiveDelete(grams, ptrs[i]);
    }
}

string SentenceBuilder::buildSentence() {
    stringstream ss;
    shared_ptr<Gram> curr = _root->_children[rand() % _root->_children.size()];
    
    while (curr->_children.size() > 0) {
        ss << curr->_tokens[curr->_tokens.size() - 1] << " ";
        uint32_t randIndex = rand() % curr->_children.size();
        curr = curr->_children[randIndex];
    }
    ss << curr->_tokens[curr->_tokens.size() - 1] << " ";
    
    return ss.str();
}

//======================================================================
// Gram
//

// vector<string> _tokens;
// vector<shared_ptr<Gram> > _children;

Gram::~Gram() {
    //cout << "Here with " << ::vecToString(_tokens) << endl;
    _children.clear();
}

string Gram::to_str() {
    return ::vecToString(_tokens);
}


//======================================================================
// Helpers
//

string vecToString(vector<string> tokens) {
    stringstream ss;
    uint32_t i;
    for (i = 0; i < tokens.size(); ++i) {
        ss << tokens[i] << " ";
    }
    return ss.str();
}





/* ------------------------------------------------- */






#ifndef SENTENCE_BUILDER_HEADER
#define SENTENCE_BUILDER_HEADER

#include <string>
#include <vector>
#include <memory>

using namespace std;

// Represents a particular N-gram.
class Gram {
public:
    vector<string> _tokens;
    vector<shared_ptr<Gram> > _children;
    
    ~Gram();
    string to_str();
};

// Represents a class that parses a file, extracts N-grams (for
// variable N) from it, and can generate random sentences based on these
// N-grams.
class SentenceBuilder {
  private:
    shared_ptr<Gram> _root;
    uint32_t _n;

    shared_ptr<Gram> startSentence(map<string, shared_ptr<Gram> >& grams, fstream& file);
    shared_ptr<Gram> GetDefaultOrAdd(map<string, shared_ptr<Gram> >& grams, vector<string> tokens);
    void recursiveDelete(vector<shared_ptr<Gram> >& grams, shared_ptr<Gram> curr);
    
  public:
    // Constructs a SentenceBuilder for a given file. The
    // SentenceBuilder will track n-grams of length n.
    SentenceBuilder(string& filename, uint32_t n);
    ~SentenceBuilder();

    // Generates a returns a random sentence. The random sentence
    // contains only n-grams from the given file. 
    string buildSentence();
};

#endif // SENTENCE_BUILDER_HEADER





/* ------------------------------------------------- */





#include <iostream>
#include <dirent.h>
#include <map>
#include <cstdlib>
#include <memory>

using namespace std;

#include "SentenceBuilder.h"

void usage();
vector<string> getNames(string dirName);

int main(int argc, char* argv[]) {
    if (argc != 3) usage();

    uint32_t gramSize = atoi(argv[1]);
    string dirName = argv[2];

    // Maps from token (such as "hugo" or "kafka") to corresponding
    // SentenceBuilder.
    map<string, shared_ptr<SentenceBuilder> > builders;

    // Get a vector of filenames from the target directory
    vector<string> names = getNames(dirName);

    // Construct all the necessary SentenceBuilders
    vector<string>::iterator nameIt;
    for (nameIt = names.begin(); nameIt != names.end(); ++nameIt) {
        string name = dirName + "/" + *nameIt;
        string shortName = *nameIt;
        shortName.erase(shortName.size() - 4);
        
        cout << "Constructing model " << shortName << endl;
        
        // Create a SentenceBuilder
        shared_ptr<SentenceBuilder> sb(new SentenceBuilder(name, gramSize));

        // Add an entry in the token -> SentenceBuilder map
        pair<string, shared_ptr<SentenceBuilder> > el(shortName, sb);
        builders.insert(el);
    }
    cout << endl;

    // Prompt user for input
    do {
        string model;
        
        cout << "Enter model name to generate sentence using that model," << endl;
        cout << "list for a list of models, or exit to exit: ";
        cin >> model;

        if (model == "exit" || builders.find(model) == builders.end()) {
            break;
        }

        shared_ptr<SentenceBuilder> sb(builders[model]);
        string sentence = sb->buildSentence();
        cout << endl << "\t" << sentence << endl << endl;
        
    } while(true);

    return EXIT_SUCCESS;
}

vector<string> getNames(string dirName) {
    vector<string> names;

    DIR* dirPtr = opendir(dirName.c_str());
    if (dirPtr == NULL) usage();

    struct dirent* dirp;
    while ((dirp = readdir(dirPtr)) != NULL) {
        string name(dirp->d_name);
        if (name != ".." && name != ".") {
            names.push_back(string(dirp->d_name));
        }
    }

    closedir(dirPtr);

    return names;
}

void usage() {
    cerr << "Usage: ./soln_ex12 N directoryname" << endl;
    exit(EXIT_FAILURE);
}
