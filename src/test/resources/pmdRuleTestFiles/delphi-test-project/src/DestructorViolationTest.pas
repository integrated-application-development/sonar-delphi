unit DestructorViolationTest;

interface

type
  TTest = class(TObject)
  public
    destructor Destroy; override;
  end;

implementation

destructor TTest.Destroy;
begin
  FreeMyStuff; // No Inherited statement, non-compliant
end;
end.