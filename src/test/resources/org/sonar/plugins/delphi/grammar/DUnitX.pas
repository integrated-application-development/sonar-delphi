unit DUnitX.AutoDetect.Console;

{ Copyright Unicode Support}

interface

(** Code snippets from https://github.com/VSoftTechnologies/DUnitX **)

type
  TValueHelper = record helper for TValue
  private
  end;
  
type
  TTestLocalMethod = TProc;

  TTestMethod = procedure of object;

  Assert = class
    //Implements could be used as identifier
    class function Implements<T : IInterface>(value : IInterface; const message : string = '' ) : T;
  end;
  
  IMyInterface = interface
    ['{9B59FF6D-7812-46A6-AFBD-89560AA639DB}']  
  end;  
  
  TDUnitXEnumerable = class(TInterfacedObject, IEnumerable)
  protected
    //function IEnumerable.GetEnumerator = GetNonGenEnumerator;
    //function GetNonGenEnumerator : IEnumerator; virtual; abstract;
  end;

  TDUnitXTestFixture = class(TWeakReferencedObject, ITestFixture,ITestFixtureInfo)
//    function ITestFixtureInfo.GetTests = ITestFixtureInfo_GetTests;
  end;
       
implementation

procedure testUnicode;
var
  vString: string;
  vTest: string;
begin
  //vString := vTest;
  vString := #1234;
  vString := 'aaaa';
  vString := '©';
end;

end.
