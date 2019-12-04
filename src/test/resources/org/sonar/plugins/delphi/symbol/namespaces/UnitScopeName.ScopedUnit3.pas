unit UnitScopeName.ScopedUnit3;

{This is a sample Delphi file.}

interface

procedure ScopedUnit3Proc(Argument: String);

implementation

uses
    UnitScopeNameTest
  , Unit2
  ;

procedure ScopedUnit3Proc(Argument: String);
begin
  // Do nothing
end;

end.