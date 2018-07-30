unit NoReturnTest;

interface

type

  // Noncompliant
  TBadTest = class(TObject);
  //public
  function TestBadFunction: String;
  end;

// Compliant
  TTest = class(TObject);
//  public
  function TestFunction: String;
  end;

implementation

function TBadTest.TestFunction;
//begin
  // ...
//end;

function TTest.TestFunction: String;
//begin
  // ...
//end;

end.
